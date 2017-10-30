package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.contexthandler.service.dto.IdentifierDto;
import gov.samhsa.c2s.contexthandler.service.dto.PolicyContainerDto;
import gov.samhsa.c2s.contexthandler.service.dto.PolicyDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;
import gov.samhsa.c2s.contexthandler.service.util.PolicyCombiningAlgIds;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "c2s.context-handler.policy-provider", havingValue = "PolicyProviderStub")
public class PolicyProviderStub implements PolicyProvider {
    public static final String MOCK_MRN = "mockMrn";
    public static final String MOCK_SSN = "mockSSN";
    public static final String MOCK_MRN_OID = "1.3.6.1.4.1.21367.13.20.200";
    public static final String OID_SSN = "2.16.840.1.113883.4.1";
    public static final String UNHANDLED_ERROR_PATIENT_ID_ROOT = "UNHANDLED_ERROR_PATIENT_ID_ROOT";
    public static final String UNHANDLED_ERROR_PATIENT_ID_EXTENSION = "UNHANDLED_ERROR_PATIENT_ID_EXTENSION";
    private static final IdentifierDto PATIENT_ID = IdentifierDto.builder().oid(MOCK_MRN_OID).value(MOCK_MRN).build();
    private static final IdentifierDto PATIENT_ID_SSN = IdentifierDto.builder().oid(OID_SSN).value(MOCK_SSN).build();
    private static final IdentifierDto UNHANDLED_ERROR_PATIENT_ID = IdentifierDto.builder().oid(UNHANDLED_ERROR_PATIENT_ID_ROOT).value(UNHANDLED_ERROR_PATIENT_ID_EXTENSION).build();

    @Autowired
    private XacmlPolicySetService xacmlPolicySetService;

    @Override
    public List<Evaluatable> getPolicies(XacmlRequestDto xacmlRequest) throws NoPolicyFoundException, PolicyProviderException {
        final IdentifierDto patientIdentifier = xacmlRequest.getPatientIdentifier();
        if (UNHANDLED_ERROR_PATIENT_ID.equals(patientIdentifier)) {
            throw new PolicyProviderException("Unhandled exception");
        } else if (PATIENT_ID.equals(patientIdentifier) || PATIENT_ID_SSN.equals(patientIdentifier)) {
            try {
                final byte[] testXacmlBytes = Files.readAllBytes(Paths.get(new ClassPathResource("sampleXacmlTemplate.xml").getURI()));
                final PolicyDto policyDto = new PolicyDto();
                policyDto.setId("consentReferenceId");
                policyDto.setPolicy(testXacmlBytes);
                final PolicyContainerDto policyContainerDto = PolicyContainerDto.builder().policies(Collections.singletonList(policyDto)).build();


                final Evaluatable policySet = xacmlPolicySetService.getPoliciesCombinedAsPolicySet(
                        policyContainerDto,
                        UUID.randomUUID().toString(),
                        PolicyCombiningAlgIds.DENY_OVERRIDES.getUrn()
                );

                return Arrays.asList(policySet);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            throw new NoPolicyFoundException("Test request content doesn't match.\nExpected: " + PATIENT_ID + "\nActual: " + patientIdentifier);
        }
    }
}