package gov.samhsa.c2s.contexthandler;

import gov.samhsa.c2s.contexthandler.service.ContextHandlerService;
import gov.samhsa.c2s.contexthandler.service.PolicyProvider;
import gov.samhsa.c2s.contexthandler.service.TestPolicyProvider;
import gov.samhsa.c2s.contexthandler.service.dto.PatientIdDto;
import gov.samhsa.c2s.contexthandler.service.dto.SubjectPurposeOfUse;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlResponseDto;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static gov.samhsa.c2s.contexthandler.service.TestPolicyProvider.PATIENT_ID_EXTENSION;
import static gov.samhsa.c2s.contexthandler.service.TestPolicyProvider.PATIENT_ID_ROOT;
import static gov.samhsa.c2s.contexthandler.service.TestPolicyProvider.UNHANDLED_ERROR_PATIENT_ID_EXTENSION;
import static gov.samhsa.c2s.contexthandler.service.TestPolicyProvider.UNHANDLED_ERROR_PATIENT_ID_ROOT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = {"default"})
public class ContextHandlerApplicationTest {
    private static final String INTERMEDIARY_NPI_1111111111 = "1111111111";
    private static final String INTERMEDIARY_NPI_2222222222 = "2222222222";
    private static final String INTERMEDIARY_NPI_3333333333 = "3333333333";
    private static final String RECIPIENT_NPI_4444444444 = "4444444444";
    private static final String RECIPIENT_NPI_5555555555 = "5555555555";
    private static final String RECIPIENT_NPI_6666666666 = "6666666666";

    private static final String INVALID_VALUE = "INVALID_VALUE";
    private static final String PERMIT = "PERMIT";
    private static final String DENY = "DENY";
    private static final List<String> PERMIT_OBLIGATIONS = Collections.unmodifiableList(Arrays.asList("SEX", "PSY", "COM", "ALC", "ETH", "HIV"));

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private PolicyProvider policyProvider;

    @Autowired
    private ContextHandlerService contextHandlerService;

    @Test
    public void contextLoads() {
    }

    @Test
    public void testInstanceOfPolicyProvider() {
        assertEquals("PolicyProvider implementation must be: " + TestPolicyProvider.class, TestPolicyProvider.class, policyProvider.getClass());
    }

    @Test
    public void testContextHandlerService_Permit_Recipient_4444444444_Intermediary_1111111111_Purpose_Treatment_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_4444444444)
                .intermediaryNpi(INTERMEDIARY_NPI_1111111111)
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientId(PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", PERMIT, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse has extra obligations that it is not supposed to have", PERMIT_OBLIGATIONS.containsAll(xacmlResponseDto.getPdpObligations()));
        assertTrue("xacmlResponse is missing obligations that it is supposed to have", xacmlResponseDto.getPdpObligations().containsAll(PERMIT_OBLIGATIONS));
    }

    @Test
    public void testContextHandlerService_Permit_Recipient_4444444444_Intermediary_1111111111_Purpose_Payment_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_4444444444)
                .intermediaryNpi(INTERMEDIARY_NPI_1111111111)
                .purposeOfUse(SubjectPurposeOfUse.PAYMENT)
                .patientId(PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", PERMIT, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse has extra obligations that it is not supposed to have", PERMIT_OBLIGATIONS.containsAll(xacmlResponseDto.getPdpObligations()));
        assertTrue("xacmlResponse is missing obligations that it is supposed to have", xacmlResponseDto.getPdpObligations().containsAll(PERMIT_OBLIGATIONS));
    }

    @Test
    public void testContextHandlerService_Permit_Recipient_5555555555_Intermediary_1111111111_Purpose_Treatment_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_5555555555)
                .intermediaryNpi(INTERMEDIARY_NPI_1111111111)
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientId(PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", PERMIT, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse has extra obligations that it is not supposed to have", PERMIT_OBLIGATIONS.containsAll(xacmlResponseDto.getPdpObligations()));
        assertTrue("xacmlResponse is missing obligations that it is supposed to have", xacmlResponseDto.getPdpObligations().containsAll(PERMIT_OBLIGATIONS));
    }

    @Test
    public void testContextHandlerService_Permit_Recipient_6666666666_Intermediary_1111111111_Purpose_Treatment_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_6666666666)
                .intermediaryNpi(INTERMEDIARY_NPI_1111111111)
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientId(PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", PERMIT, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse has extra obligations that it is not supposed to have", PERMIT_OBLIGATIONS.containsAll(xacmlResponseDto.getPdpObligations()));
        assertTrue("xacmlResponse is missing obligations that it is supposed to have", xacmlResponseDto.getPdpObligations().containsAll(PERMIT_OBLIGATIONS));
    }

    @Test
    public void testContextHandlerService_Permit_Recipient_4444444444_Intermediary_2222222222_Purpose_Treatment_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_4444444444)
                .intermediaryNpi(INTERMEDIARY_NPI_2222222222)
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientId(PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", PERMIT, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse has extra obligations that it is not supposed to have", PERMIT_OBLIGATIONS.containsAll(xacmlResponseDto.getPdpObligations()));
        assertTrue("xacmlResponse is missing obligations that it is supposed to have", xacmlResponseDto.getPdpObligations().containsAll(PERMIT_OBLIGATIONS));
    }

    @Test
    public void testContextHandlerService_Permit_Recipient_5555555555_Intermediary_2222222222_Purpose_Treatment_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_5555555555)
                .intermediaryNpi(INTERMEDIARY_NPI_2222222222)
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientId(PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", PERMIT, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse has extra obligations that it is not supposed to have", PERMIT_OBLIGATIONS.containsAll(xacmlResponseDto.getPdpObligations()));
        assertTrue("xacmlResponse is missing obligations that it is supposed to have", xacmlResponseDto.getPdpObligations().containsAll(PERMIT_OBLIGATIONS));
    }

    @Test
    public void testContextHandlerService_Permit_Recipient_6666666666_Intermediary_2222222222_Purpose_Treatment_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_6666666666)
                .intermediaryNpi(INTERMEDIARY_NPI_2222222222)
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientId(PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", PERMIT, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse has extra obligations that it is not supposed to have", PERMIT_OBLIGATIONS.containsAll(xacmlResponseDto.getPdpObligations()));
        assertTrue("xacmlResponse is missing obligations that it is supposed to have", xacmlResponseDto.getPdpObligations().containsAll(PERMIT_OBLIGATIONS));
    }

    @Test
    public void testContextHandlerService_Permit_Recipient_4444444444_Intermediary_3333333333_Purpose_Treatment_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_4444444444)
                .intermediaryNpi(INTERMEDIARY_NPI_3333333333)
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientId(PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", PERMIT, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse has extra obligations that it is not supposed to have", PERMIT_OBLIGATIONS.containsAll(xacmlResponseDto.getPdpObligations()));
        assertTrue("xacmlResponse is missing obligations that it is supposed to have", xacmlResponseDto.getPdpObligations().containsAll(PERMIT_OBLIGATIONS));
    }

    @Test
    public void testContextHandlerService_Permit_Recipient_5555555555_Intermediary_3333333333_Purpose_Treatment_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_5555555555)
                .intermediaryNpi(INTERMEDIARY_NPI_3333333333)
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientId(PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", PERMIT, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse has extra obligations that it is not supposed to have", PERMIT_OBLIGATIONS.containsAll(xacmlResponseDto.getPdpObligations()));
        assertTrue("xacmlResponse is missing obligations that it is supposed to have", xacmlResponseDto.getPdpObligations().containsAll(PERMIT_OBLIGATIONS));
    }

    @Test
    public void testContextHandlerService_Permit_Recipient_6666666666_Intermediary_3333333333_Purpose_Treatment_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_6666666666)
                .intermediaryNpi(INTERMEDIARY_NPI_3333333333)
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientId(PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", PERMIT, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse has extra obligations that it is not supposed to have", PERMIT_OBLIGATIONS.containsAll(xacmlResponseDto.getPdpObligations()));
        assertTrue("xacmlResponse is missing obligations that it is supposed to have", xacmlResponseDto.getPdpObligations().containsAll(PERMIT_OBLIGATIONS));
    }

    @Test
    public void testContextHandlerService_Deny_Recipient_INVALID_Intermediary_3333333333_Purpose_Treatment_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(INVALID_VALUE)
                .intermediaryNpi(INTERMEDIARY_NPI_3333333333)
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientId(PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", DENY, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse must not have any obligations when the decision is DENY", xacmlResponseDto.getPdpObligations().isEmpty());
    }

    @Test
    public void testContextHandlerService_Deny_Recipient_6666666666_Intermediary_INVALID_Purpose_Treatment_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_6666666666)
                .intermediaryNpi(INVALID_VALUE)
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientId(PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", DENY, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse must not have any obligations when the decision is DENY", xacmlResponseDto.getPdpObligations().isEmpty());
    }

    @Test
    public void testContextHandlerService_Deny_Recipient_6666666666_Intermediary_3333333333_Purpose_INVALID_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_6666666666)
                .intermediaryNpi(INTERMEDIARY_NPI_3333333333)
                .purposeOfUse(SubjectPurposeOfUse.RESEARCH)
                .patientId(PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", DENY, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse must not have any obligations when the decision is DENY", xacmlResponseDto.getPdpObligations().isEmpty());
    }

    @Test
    public void testContextHandlerService_Deny_Recipient_6666666666_Intermediary_3333333333_Purpose_Treatment_Patient_InvalidByRoot() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_6666666666)
                .intermediaryNpi(INTERMEDIARY_NPI_3333333333)
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientId(PatientIdDto.builder().root(INVALID_VALUE).extension(PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", DENY, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse must not have any obligations when the decision is DENY", xacmlResponseDto.getPdpObligations().isEmpty());
    }

    @Test
    public void testContextHandlerService_Deny_Recipient_6666666666_Intermediary_3333333333_Purpose_Treatment_Patient_InvalidByExtension() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_6666666666)
                .intermediaryNpi(INTERMEDIARY_NPI_3333333333)
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientId(PatientIdDto.builder().root(PATIENT_ID_ROOT).extension(INVALID_VALUE).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", DENY, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse must not have any obligations when the decision is DENY", xacmlResponseDto.getPdpObligations().isEmpty());
    }

    @Test
    public void testContextHandlerService_Throws_PolicyProviderException() {
        // Arrange
        thrown.expect(PolicyProviderException.class);
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientNpi(RECIPIENT_NPI_6666666666)
                .intermediaryNpi(INTERMEDIARY_NPI_3333333333)
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientId(PatientIdDto.builder().root(UNHANDLED_ERROR_PATIENT_ID_ROOT).extension(UNHANDLED_ERROR_PATIENT_ID_EXTENSION).build())
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);
    }
}