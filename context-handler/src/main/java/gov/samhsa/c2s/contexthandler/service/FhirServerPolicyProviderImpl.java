package gov.samhsa.c2s.contexthandler.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.gclient.DateClientParam;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import gov.samhsa.c2s.common.consentgen.ConsentBuilder;
import gov.samhsa.c2s.common.consentgen.ConsentDto;
import gov.samhsa.c2s.common.consentgen.ConsentGenException;
import gov.samhsa.c2s.common.consentgen.PatientDto;
import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.contexthandler.config.FhirProperties;
import gov.samhsa.c2s.contexthandler.service.dto.ConsentBundleAndPatientDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.exception.ConsentNotFound;
import gov.samhsa.c2s.contexthandler.service.exception.MockFileReadException;
import gov.samhsa.c2s.contexthandler.service.exception.MultiplePatientsFound;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PatientNotFound;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;
import org.codehaus.jackson.map.ObjectMapper;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@ConditionalOnBean(FhirProperties.class)
public class FhirServerPolicyProviderImpl implements PolicyProvider {
    private static final String MOCK_FHIR_CONSENT_FILENAME = "mockFhirConsent.json";

    private final ConsentBuilder consentBuilder;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FhirContext fhirContext;

    @Autowired
    private IGenericClient fhirClient;

    @Autowired
    private FhirProperties fhirProperties;

    @Autowired
    public FhirServerPolicyProviderImpl(ConsentBuilder consentBuilder) {
        this.consentBuilder = consentBuilder;
    }

    @Override
    public List<Evaluatable> getPolicies(XacmlRequestDto xacmlRequest) throws NoPolicyFoundException, PolicyProviderException {
        ConsentDto consentDto;

        ConsentBundleAndPatientDto consentBundleAndPatientDto = tempGetFhirConsent(xacmlRequest.getPatientId().getExtension());

        Patient fhirPatient = consentBundleAndPatientDto.getPatient();

        // FIXME: Temporarily only use first consent in bundle
        Consent fhirConsent = (Consent) consentBundleAndPatientDto.getConsentSearchResponse().getEntry().get(0).getResource();

        logger.info("FHIR CONSENT: " + fhirConsent.toString());

        try {
            consentDto = consentBuilder.buildFhirConsent2ConsentDto(fhirConsent, fhirPatient);
            logger.info("Conversion of FHIR Consent to ConsentDto complete.");

            if(consentDto.getProvidersPermittedToDisclose().size() > 0) {
                logger.info("INDIVIDUAL FROM PROVIDER(S):");
                consentDto.getProvidersPermittedToDisclose()
                        .forEach(individualProviderDto -> logger.info(individualProviderDto.getNpi()));
            }

            if(consentDto.getOrganizationalProvidersPermittedToDisclose().size() > 0) {
                logger.info("ORGANIZATIONAL FROM PROVIDER(S):");
                consentDto.getOrganizationalProvidersPermittedToDisclose()
                        .forEach(organizationalProviderDto -> logger.info(organizationalProviderDto.getNpi()));
            }

            if(consentDto.getProvidersDisclosureIsMadeTo().size() > 0){
                logger.info("INDIVIDUAL TO PROVIDER(S):");
                consentDto.getProvidersDisclosureIsMadeTo()
                        .forEach(individualProviderDto -> logger.info(individualProviderDto.getNpi()));
            }

            if(consentDto.getOrganizationalProvidersDisclosureIsMadeTo().size() > 0) {
                logger.info("ORGANIZATIONAL TO PROVIDER(S):");
                consentDto.getOrganizationalProvidersDisclosureIsMadeTo()
                        .forEach(organizationalProviderDto -> logger.info(organizationalProviderDto.getNpi()));
            }

        }catch (ConsentGenException e){
            logger.error("ConsentGenException occurred while trying to convert FHIR Consent object to ConsentDto object", e);
            throw new PolicyProviderException("Unable to process FHIR consent", e);
        }

        logger.info("CONSENT DTO OBJECT ID & DATES: " + consentDto.getConsentReferenceid() + "; " + consentDto.getConsentStart() + "; " + consentDto.getConsentEnd() + "; " + consentDto.getSignedDate());
        logger.info("CONSENT SHARE SENSITIVITY POLICY CODES: " + consentDto.getShareSensitivityPolicyCodes().stream().map(tcd -> tcd.getCode() + " - " + tcd.getCodeSystem() + ", ").reduce("", String::concat));
        logger.info("CONSENT SHARE FOR PURPOSE OF USE CODES: " + consentDto.getShareForPurposeOfUseCodes().stream().map(tcd -> tcd.getCode() + " - " + tcd.getCodeSystem() + ", ").reduce("", String::concat));
        logger.info("CONSENT PATIENT NAME: " + consentDto.getPatientDto().getFirstName() + " " + consentDto.getPatientDto().getLastName());
        logger.info("CONSENT PATIENT MRN: " + consentDto.getPatientDto().getMedicalRecordNumber());

        consentDto.setLegalRepresentative(new PatientDto());
        consentDto.setVersion(0);

        PatientDto patientDto = consentDto.getPatientDto();
        patientDto.setPatientIdNumber("");
        patientDto.setAddressCity("");
        patientDto.setAddressCountryCode("");
        patientDto.setAddressPostalCode("");
        patientDto.setAddressStateCode("");
        patientDto.setAddressStreetAddressLine("");
        patientDto.setAdministrativeGenderCode("");
        patientDto.setBirthDate(new Date());
        patientDto.setEmail("");
        patientDto.setEnterpriseIdentifier("");
        patientDto.setPrefix("");
        patientDto.setSocialSecurityNumber("");
        patientDto.setTelephoneTypeTelephone("");

        consentDto.setPatientDto(patientDto);

        String consentXacmlString;

        try{
            consentXacmlString = consentBuilder.buildConsent2Xacml(consentDto);
        }catch (ConsentGenException e){
            logger.error("ConsentGenException occurred while trying to convert ConsentDto object to XACML", e);
            throw new PolicyProviderException("Unable to process FHIR consent", e);
        }

        logger.info("XACML:");
        logger.info(consentXacmlString);

        return null;
    }


    private Consent getMockFhirConsentObject(){
        ClassLoader classLoader = getClass().getClassLoader();
        ObjectMapper objectMapper = new ObjectMapper();
        File file;
        Consent fhirConsent;

        try{
            //noinspection ConstantConditions
            file = new File(classLoader.getResource(MOCK_FHIR_CONSENT_FILENAME).getFile());
        }catch(NullPointerException e){
            logger.error("Error getting mock file", e);
            throw new MockFileReadException(e);
        }

        try(FileReader fileReader = new FileReader(file)){
            fhirConsent = fhirContext.newJsonParser().parseResource(Consent.class, fileReader);
            logger.info("Mock FHIR consent successfully read from file and mapped to FHIR Consent object.");
        }catch(IOException e){
            logger.error("Error reading mock file from input stream", e);
            throw new MockFileReadException(e);
        }

        return fhirConsent;
    }

    //temp method
    @Override
    public ConsentBundleAndPatientDto tempGetFhirConsent(String mrn){
        String mrnSystem = fhirProperties.getMrn().getSystem();

        Bundle patientSearchResponse = fhirClient.search()
                .forResource(Patient.class)
                .where(new TokenClientParam("identifier")
                        .exactly()
                        .systemAndCode(mrnSystem, mrn))
                .returnBundle(Bundle.class)
                .execute();

        if(patientSearchResponse == null || patientSearchResponse.getEntry().size() < 1){
            throw new PatientNotFound("No patient found for the given MRN:" + mrn);
        }

        if(patientSearchResponse.getEntry().size() > 1){
            throw new MultiplePatientsFound("Multiple patients found for the given MRN:" + mrn);
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

        if(consentSearchResponse == null || consentSearchResponse.getEntry().size() < 1){
            throw new ConsentNotFound("No active consent found for date:" + dateToday + " and for the given MRN:" + mrn);
        }

        return new ConsentBundleAndPatientDto(consentSearchResponse, patientObj);
    }

}
