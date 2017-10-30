package gov.samhsa.c2s.contexthandler;

import gov.samhsa.c2s.contexthandler.service.ContextHandlerService;
import gov.samhsa.c2s.contexthandler.service.PolicyProvider;
import gov.samhsa.c2s.contexthandler.service.PolicyProviderStub;
import gov.samhsa.c2s.contexthandler.service.dto.IdentifierDto;
import gov.samhsa.c2s.contexthandler.service.dto.ObligationDto;
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

import static gov.samhsa.c2s.contexthandler.service.PolicyProviderStub.MOCK_MRN;
import static gov.samhsa.c2s.contexthandler.service.PolicyProviderStub.MOCK_MRN_OID;
import static gov.samhsa.c2s.contexthandler.service.PolicyProviderStub.MOCK_SSN;
import static gov.samhsa.c2s.contexthandler.service.PolicyProviderStub.OID_SSN;
import static gov.samhsa.c2s.contexthandler.service.PolicyProviderStub.UNHANDLED_ERROR_PATIENT_ID_EXTENSION;
import static gov.samhsa.c2s.contexthandler.service.PolicyProviderStub.UNHANDLED_ERROR_PATIENT_ID_ROOT;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = {"default"})
public class ContextHandlerApplicationTest {
    private static final String URN_SAMHSA_NAMES_TC_CONSENT2SHARE_1_0_OBLIGATION_SHARE_SENSITIVITY_POLICY_CODE = "urn:samhsa:names:tc:consent2share:1.0:obligation:share-sensitivity-policy-code";
    private static final String OID_NPI = "2.16.840.1.113883.4.6";
    private static final String INTERMEDIARY_NPI_1111111111 = "1111111111";
    private static final String INTERMEDIARY_NPI_2222222222 = "2222222222";
    private static final String INTERMEDIARY_NPI_3333333333 = "3333333333";
    private static final String RECIPIENT_NPI_4444444444 = "4444444444";
    private static final String RECIPIENT_NPI_5555555555 = "5555555555";
    private static final String RECIPIENT_NPI_6666666666 = "6666666666";

    private static final String INVALID_VALUE = "INVALID_VALUE";
    private static final String PERMIT = "PERMIT";
    private static final String DENY = "DENY";
    private static final List<ObligationDto> PERMIT_OBLIGATIONS = Collections.unmodifiableList(Arrays.asList("SEX", "PSY", "COM", "ALC", "ETH", "HIV")).stream()
            .map(obligationValue -> ObligationDto.of(URN_SAMHSA_NAMES_TC_CONSENT2SHARE_1_0_OBLIGATION_SHARE_SENSITIVITY_POLICY_CODE, obligationValue))
            .collect(toList());
    private static final String ACTION_ID_PEPACCESS = "pepaccess";

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
        assertEquals("PolicyProvider implementation must be: " + PolicyProviderStub.class, PolicyProviderStub.class, policyProvider.getClass());
    }

    @Test
    public void testContextHandlerService_Permit_Recipient_4444444444_Intermediary_1111111111_Purpose_Treatment_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_4444444444))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_1111111111))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(MOCK_MRN_OID, MOCK_MRN))
                .actionId(ACTION_ID_PEPACCESS)
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", PERMIT, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse has extra obligations that it is not supposed to have", PERMIT_OBLIGATIONS.containsAll(xacmlResponseDto.getPdpObligations()));
        assertTrue("xacmlResponse is missing obligations that it is supposed to have", xacmlResponseDto.getPdpObligations().containsAll(PERMIT_OBLIGATIONS));
    }

    @Test
    public void testContextHandlerService_Permit_Recipient_4444444444_Intermediary_1111111111_Purpose_Treatment_Patient_Correct_SSN() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_4444444444))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_1111111111))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(OID_SSN, MOCK_SSN))
                .actionId(ACTION_ID_PEPACCESS)
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", PERMIT, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse has extra obligations that it is not supposed to have", PERMIT_OBLIGATIONS.containsAll(xacmlResponseDto.getPdpObligations()));
        assertTrue("xacmlResponse is missing obligations that it is supposed to have", xacmlResponseDto.getPdpObligations().containsAll(PERMIT_OBLIGATIONS));
    }

    @Test
    public void testContextHandlerService_Deny_Recipient_4444444444_Intermediary_1111111111_Purpose_Treatment_Patient_SSNInvalidByOid() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_4444444444))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_1111111111))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(INVALID_VALUE, MOCK_SSN))
                .actionId(ACTION_ID_PEPACCESS)
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", DENY, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse is not supposed to have any obligations when the decision is deny", xacmlResponseDto.getPdpObligations().isEmpty());
    }

    @Test
    public void testContextHandlerService_Deny_Recipient_4444444444_Intermediary_1111111111_Purpose_Treatment_Patient_SSNInvalidByValue() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_4444444444))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_1111111111))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(OID_SSN, INVALID_VALUE))
                .actionId(ACTION_ID_PEPACCESS)
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);

        // Assert
        assertEquals("Decision doesn't match", DENY, xacmlResponseDto.getPdpDecision());
        assertTrue("xacmlResponse is not supposed to have any obligations when the decision is deny", xacmlResponseDto.getPdpObligations().isEmpty());
    }

    @Test
    public void testContextHandlerService_Permit_Recipient_4444444444_Intermediary_1111111111_Purpose_Payment_Patient_Correct() {
        // Arrange
        final XacmlRequestDto xacmlRequest = XacmlRequestDto.builder()
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_4444444444))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_1111111111))
                .purposeOfUse(SubjectPurposeOfUse.PAYMENT)
                .patientIdentifier(IdentifierDto.of(MOCK_MRN_OID, MOCK_MRN))
                .actionId(ACTION_ID_PEPACCESS)
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
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_5555555555))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_1111111111))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(MOCK_MRN_OID, MOCK_MRN))
                .actionId(ACTION_ID_PEPACCESS)
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
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_6666666666))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_1111111111))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(MOCK_MRN_OID, MOCK_MRN))
                .actionId(ACTION_ID_PEPACCESS)
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
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_4444444444))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_2222222222))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(MOCK_MRN_OID, MOCK_MRN))
                .actionId(ACTION_ID_PEPACCESS)
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
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_5555555555))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_2222222222))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(MOCK_MRN_OID, MOCK_MRN))
                .actionId(ACTION_ID_PEPACCESS)
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
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_6666666666))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_2222222222))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(MOCK_MRN_OID, MOCK_MRN))
                .actionId(ACTION_ID_PEPACCESS)
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
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_4444444444))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_3333333333))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(MOCK_MRN_OID, MOCK_MRN))
                .actionId(ACTION_ID_PEPACCESS)
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
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_5555555555))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_3333333333))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(MOCK_MRN_OID, MOCK_MRN))
                .actionId(ACTION_ID_PEPACCESS)
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
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_6666666666))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_3333333333))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(MOCK_MRN_OID, MOCK_MRN))
                .actionId(ACTION_ID_PEPACCESS)
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
                .recipientIdentifier(IdentifierDto.of(OID_NPI, INVALID_VALUE))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_3333333333))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(MOCK_MRN_OID, MOCK_MRN))
                .actionId(ACTION_ID_PEPACCESS)
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
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_6666666666))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INVALID_VALUE))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(MOCK_MRN_OID, MOCK_MRN))
                .actionId(ACTION_ID_PEPACCESS)
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
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_6666666666))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_3333333333))
                .purposeOfUse(SubjectPurposeOfUse.RESEARCH)
                .patientIdentifier(IdentifierDto.of(MOCK_MRN_OID, MOCK_MRN))
                .actionId(ACTION_ID_PEPACCESS)
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
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_6666666666))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_3333333333))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(INVALID_VALUE, MOCK_MRN_OID))
                .actionId(ACTION_ID_PEPACCESS)
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
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_6666666666))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_3333333333))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(MOCK_MRN, INVALID_VALUE))
                .actionId(ACTION_ID_PEPACCESS)
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
                .recipientIdentifier(IdentifierDto.of(OID_NPI, RECIPIENT_NPI_6666666666))
                .intermediaryIdentifier(IdentifierDto.of(OID_NPI, INTERMEDIARY_NPI_3333333333))
                .purposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT)
                .patientIdentifier(IdentifierDto.of(UNHANDLED_ERROR_PATIENT_ID_ROOT, UNHANDLED_ERROR_PATIENT_ID_EXTENSION))
                .actionId(ACTION_ID_PEPACCESS)
                .build();

        // Act
        final XacmlResponseDto xacmlResponseDto = contextHandlerService.enforcePolicy(xacmlRequest);
    }
}