package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.contexthandler.service.dto.PatientIdDto;
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
    public static final String PATIENT_ID_ROOT = "PATIENT_ID_ROOT";
    public static final String PATIENT_ID_EXTENSION = "PATIENT_ID_EXTENSION";
    public static final String UNHANDLED_ERROR_PATIENT_ID_ROOT = "UNHANDLED_ERROR_PATIENT_ID_ROOT";
    public static final String UNHANDLED_ERROR_PATIENT_ID_EXTENSION = "UNHANDLED_ERROR_PATIENT_ID_EXTENSION";
    public static final PatientIdDto PATIENT_ID = PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(PATIENT_ID_EXTENSION).build();
    public static final PatientIdDto UNHANDLED_ERROR_PATIENT_ID = PatientIdDto.builder().root(UNHANDLED_ERROR_PATIENT_ID_ROOT).extension(UNHANDLED_ERROR_PATIENT_ID_EXTENSION).build();

    @Autowired
    private XacmlPolicySetService xacmlPolicySetService;

    @Override
    public List<Evaluatable> getPolicies(XacmlRequestDto xacmlRequest) throws NoPolicyFoundException, PolicyProviderException {
        if (UNHANDLED_ERROR_PATIENT_ID.equals(xacmlRequest.getPatientId())) {
            throw new PolicyProviderException("Unhandled exception");
        } else if (PATIENT_ID.equals(xacmlRequest.getPatientId())) {
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
            throw new NoPolicyFoundException("Test request content doesn't match.\nExpected: " + PATIENT_ID + "\nActual: " + xacmlRequest.getPatientId());
        }
    }
}