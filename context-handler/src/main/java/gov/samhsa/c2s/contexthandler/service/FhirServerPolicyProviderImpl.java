package gov.samhsa.c2s.contexthandler.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.DateClientParam;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import gov.samhsa.c2s.common.consentgen.ConsentBuilder;
import gov.samhsa.c2s.common.consentgen.ConsentDto;
import gov.samhsa.c2s.common.consentgen.ConsentGenException;
import gov.samhsa.c2s.common.consentgen.IdentifierDto;
import gov.samhsa.c2s.contexthandler.service.dto.ConsentListAndPatientDto;
import gov.samhsa.c2s.contexthandler.service.dto.PolicyContainerDto;
import gov.samhsa.c2s.contexthandler.service.dto.PolicyDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.exception.FhirConsentInvalidException;
import gov.samhsa.c2s.contexthandler.service.exception.MultiplePatientsFound;
import gov.samhsa.c2s.contexthandler.service.exception.NoConsentFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PatientNotFound;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;
import gov.samhsa.c2s.contexthandler.service.util.PolicyCombiningAlgIds;
import lombok.extern.slf4j.Slf4j;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.r4.model.codesystems.V3ParticipationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@ConditionalOnProperty(name = "c2s.context-handler.policy-provider", havingValue = "FhirServerPolicyProviderImpl")
public class FhirServerPolicyProviderImpl implements PolicyProvider {
    private final ConsentBuilder consentBuilder;
    private final XacmlPolicySetService xacmlPolicySetService;
    @Autowired
    private IGenericClient fhirClient;

    @Autowired
    public FhirServerPolicyProviderImpl(ConsentBuilder consentBuilder, XacmlPolicySetService xacmlPolicySetService) {
        this.consentBuilder = consentBuilder;
        this.xacmlPolicySetService = xacmlPolicySetService;
    }

    @Override
    public List<Evaluatable> getPolicies(XacmlRequestDto xacmlRequest) throws NoPolicyFoundException, PolicyProviderException {
        ConsentListAndPatientDto consentListAndPatientDto = searchForFhirPatientAndFhirConsent(xacmlRequest);
        Patient fhirPatient = consentListAndPatientDto.getPatient();

        List<Consent> fhirConsentList = consentListAndPatientDto.getMatchingConsents();
        if (Objects.isNull(fhirConsentList) || fhirConsentList.isEmpty()) {
            throw new NoPolicyFoundException("Consent not found with given XACML Request" + xacmlRequest);
        }

        List<ConsentDto> consentDtoList = convertFhirConsentListToConsentDtoList(fhirConsentList, fhirPatient);
        List<PolicyDto> policyDtoList = convertConsentDtoListToXacmlPolicyDtoList(consentDtoList);

        PolicyContainerDto policyContainerDto = PolicyContainerDto.builder().policies(policyDtoList).build();

        Evaluatable policySet = xacmlPolicySetService.getPoliciesCombinedAsPolicySet(
                policyContainerDto,
                UUID.randomUUID().toString(),
                PolicyCombiningAlgIds.DENY_OVERRIDES.getUrn()
        );

        return Arrays.asList(policySet);
    }

    private List<PolicyDto> convertConsentDtoListToXacmlPolicyDtoList(List<ConsentDto> consentDtoList) {
        List<PolicyDto> policyDtoList = new ArrayList<>();

        try {
            for (ConsentDto consentDto : consentDtoList) {
                String consentXacmlString = consentBuilder.buildConsent2Xacml(consentDto);
                PolicyDto policyDto = new PolicyDto();
                policyDto.setId(consentDto.getConsentReferenceid());
                policyDto.setPolicy(consentXacmlString.getBytes(StandardCharsets.UTF_8));

                policyDtoList.add(policyDto);
            }
            log.info("Conversion of ConsentDto list to XACML PolicyDto list complete.");
        } catch (ConsentGenException e) {
            log.error("ConsentGenException occurred while trying to convert ConsentDto object(s) to XACML PolicyDto object(s)", e);
            throw new PolicyProviderException("Unable to process FHIR consent(s)", e);
        }

        return policyDtoList;
    }

    private List<ConsentDto> convertFhirConsentListToConsentDtoList(List<Consent> fhirConsentList, Patient fhirPatient) {
        List<ConsentDto> consentDtoList = new ArrayList<>();

        try {
            for (Consent fhirConsent : fhirConsentList) {
                consentDtoList.add(consentBuilder.buildFhirConsent2ConsentDto(fhirConsent, fhirPatient));
            }
            log.info("Conversion of FHIR Consent list to ConsentDto list complete.");
        } catch (ConsentGenException e) {
            log.error("ConsentGenException occurred while trying to convert FHIR Consent object(s) to ConsentDto object(s)", e);
            throw new PolicyProviderException("Unable to process FHIR consent(s)", e);
        }

        return consentDtoList;
    }

    private ConsentListAndPatientDto searchForFhirPatientAndFhirConsent(XacmlRequestDto xacmlRequest) {
        final String patientMrnSystem = "urn:oid:" + xacmlRequest.getPatientIdentifier().getOid();
        final String patientMrn = xacmlRequest.getPatientIdentifier().getValue();

        Bundle patientSearchResponse = fhirClient.search()
                .forResource(Patient.class)
                .where(new TokenClientParam("identifier")
                        .exactly()
                        .systemAndCode(patientMrnSystem, patientMrn))
                .returnBundle(Bundle.class)
                .execute();

        if (patientSearchResponse == null || patientSearchResponse.getEntry().size() < 1) {
            log.debug("No patient found in FHIR server with the given MRN: " + patientMrn);
            throw new PatientNotFound("No patient found for the given MRN");
        }

        if (patientSearchResponse.getEntry().size() > 1) {
            log.warn("Multiple patients were found in FHIR server for the same given MRN: " + patientMrn);
            log.debug("       URL of FHIR Server: " + fhirClient.getServerBase());
            throw new MultiplePatientsFound("Multiple patients found in FHIR server with the given MRN");
        }

        Patient patientObj = (Patient) patientSearchResponse.getEntry().get(0).getResource();

        String patientResourceId = patientObj.getIdElement().getIdPart();
        String dateToday = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        Bundle consentSearchResponse = fhirClient.search()
                .forResource(Consent.class)
                .where(new ReferenceClientParam("patient")
                        .hasId(patientResourceId))
                .where(new TokenClientParam("status").exactly().code("active"))
                .where(new DateClientParam("period").afterOrEquals().second(dateToday))
                .where(new DateClientParam("period").beforeOrEquals().second(dateToday))
                .returnBundle(Bundle.class)
                .execute();

        if (consentSearchResponse == null || consentSearchResponse.getEntry().size() < 1) {
            log.debug("No active consents matching query parameters were found in FHIR server during search in 'searchForFhirPatientAndFhirConsent' method");
            throw new NoConsentFoundException("No active consent found for date:" + dateToday + " and for the given MRN:" + patientMrn);
        }

        log.debug("FHIR Consent(s) bundle retrieved from FHIR server successfully");

        List<Consent> matchingConsents = filterMatchingConsentsFromBundle(consentSearchResponse, xacmlRequest);

        return new ConsentListAndPatientDto(matchingConsents, patientObj);
    }

    private List<Consent> filterMatchingConsentsFromBundle(Bundle consentSearchResponse, XacmlRequestDto xacmlRequest) {
        //Loop through the consents to filter out those that match the xacmlRequest

        List<Bundle.BundleEntryComponent> retrievedConsents = consentSearchResponse.getEntry();
        List<Consent> matchingConsents = new ArrayList<>();

        for (Bundle.BundleEntryComponent tempBundleEntryComponent : retrievedConsents) {

            boolean fromProviderMatched = false;
            boolean toProviderMatched = false;
            boolean purposeOfUseMatched = false;

            Consent tempConsent = (Consent) tempBundleEntryComponent.getResource();

            //Check "To" Provider
            List<DomainResource> fhirToProviderResourceList = new ArrayList<>();

            if (tempConsent.hasActor()) {
                List<Consent.ConsentActorComponent> fhirToProviderActorList = tempConsent.getActor().stream()
                        .filter(Consent.ConsentActorComponent::hasRole)
                        .filter(actor -> actor.getRole().getCoding().stream()
                                .anyMatch(coding -> coding.getCode().equalsIgnoreCase(V3ParticipationType.IRCP.toCode())))
                        .collect(Collectors.toList());

                fhirToProviderActorList.forEach(fhirToProviderReference ->
                        fhirToProviderResourceList.add((DomainResource) fhirToProviderReference.getReference().getResource()));
            } else {
                log.error("One or more FHIR Consents in bundle passed to 'filterMatchingConsentsFromBundle' does not have any actor(s) specified");
                throw new FhirConsentInvalidException("The FHIR consent does not have any actor(s) specified");
            }
            if (fhirToProviderResourceList == null || fhirToProviderResourceList.size() < 1) {
                throw new FhirConsentInvalidException("The FHIR consent does not have any TO provider(s) specified");
            }

            for (DomainResource fhirToProviderResource : fhirToProviderResourceList) {

                Set<IdentifierDto> fhirToProviderIdentifiers;
                try {
                    fhirToProviderIdentifiers = consentBuilder.extractIdentifiersFromFhirProviderResource(fhirToProviderResource);
                } catch (ConsentGenException e) {
                    log.error("ConsentGenException occurred while attempting to extract identifiers from recipient(actor) FHIR provider resource in 'filterMatchingConsentsFromBundle' method", e);
                    throw new FhirConsentInvalidException("Error extracting identifiers from recipient(actor) Provider resource", e);
                }

                if (fhirToProviderIdentifiers.stream()
                        .anyMatch(identifierDto -> xacmlRequest.getRecipientIdentifier().getOid().equals(identifierDto.getOid()) &&
                                xacmlRequest.getRecipientIdentifier().getValue().equals(identifierDto.getValue()))) {
                    toProviderMatched = true;
                    break;
                }

            }

            //Check "From" Provider
            List<DomainResource> fhirFromProviderResourceList = new ArrayList<>();

            List<Consent.ConsentActorComponent> fhirFromProviderActorList = tempConsent.getActor().stream()
                    .filter(Consent.ConsentActorComponent::hasRole)
                    .filter(actor -> actor.getRole().getCoding().stream()
                            .anyMatch(coding -> coding.getCode().equalsIgnoreCase(V3ParticipationType.INF.toCode())))
                    .collect(Collectors.toList());

            fhirFromProviderActorList.forEach(fhirFromProvider ->
                    fhirFromProviderResourceList.add((DomainResource) fhirFromProvider.getReference().getResource()));

            for (DomainResource fhirFromProviderResource : fhirFromProviderResourceList) {
                Set<IdentifierDto> fhirFromProviderIdentifiers;
                try {
                    fhirFromProviderIdentifiers = consentBuilder.extractIdentifiersFromFhirProviderResource(fhirFromProviderResource);
                } catch (ConsentGenException e) {
                    log.error("ConsentGenException occurred while attempting to extract identifiers from organization(From) FHIR provider resource in 'filterMatchingConsentsFromBundle' method", e);
                    throw new FhirConsentInvalidException("Error extracting identifiers from organization(From) Provider resource", e);
                }

                if (fhirFromProviderIdentifiers.stream()
                        .anyMatch(identifierDto -> xacmlRequest.getIntermediaryIdentifier().getOid().equals(identifierDto.getOid()) &&
                                xacmlRequest.getIntermediaryIdentifier().getValue().equals(identifierDto.getValue()))) {
                    fromProviderMatched = true;
                }
            }

            //Check purpose of use
            if (tempConsent.hasPurpose()) {
                int filteredPurposeSize = tempConsent.getPurpose().stream()
                        .filter(pou -> pou.getCode().equals(xacmlRequest.getPurposeOfUse().getPurposeFhir()))
                        .collect(Collectors.toList()).size();

                if (filteredPurposeSize > 0) {
                    purposeOfUseMatched = true;
                }
            }

            //Add to the list only when all the checks are passed
            if (toProviderMatched && fromProviderMatched && purposeOfUseMatched) {
                matchingConsents.add(tempConsent);
            }
        }

        log.debug("FHIR Consent(s) bundle successfully filtered based on XacmlRequest object fields");

        return matchingConsents;
    }
}
