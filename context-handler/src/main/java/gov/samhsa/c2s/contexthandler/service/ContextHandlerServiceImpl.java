package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.contexthandler.service.dto.ConsentBundleAndPatientDto;
import gov.samhsa.c2s.contexthandler.service.dto.PatientIdDto;
import gov.samhsa.c2s.contexthandler.service.dto.SubjectPurposeOfUse;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlResponseDto;
import gov.samhsa.c2s.contexthandler.service.exception.C2SAuditException;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ContextHandlerServiceImpl implements ContextHandlerService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The policy desicion point.
     */
    @Autowired
    private PolicyDecisionPointService policyDesicionPointService;

    @Autowired
    private PolicyProvider policyProvider;

    @Override
    public XacmlResponseDto enforcePolicy(XacmlRequestDto xacmlRequest) throws C2SAuditException {
        try {
            logger.debug("policyDesicionPoint.evaluateRequest(xacmlRequest) is invoked");

            xacmlRequest.setMessageId(UUID.randomUUID().toString());
            final XacmlResponseDto xacmlResponse = policyDesicionPointService
                    .evaluateRequest(xacmlRequest);
            logger.debug(() -> "PDP Decision: " + xacmlResponse.getPdpDecision());
            logger.debug(() -> "PDP Obligations: " + xacmlResponse.getPdpObligations().toString());
            return xacmlResponse;
        } catch (final NoPolicyFoundException e) {
            List<String> pdpObligations = new ArrayList<>();
            final XacmlResponseDto xacmlResponse = XacmlResponseDto.builder().pdpDecision("DENY").pdpObligations(pdpObligations).build();
            xacmlResponse.setPdpDecision(e.getClass().getName());
            return xacmlResponse;
        }
    }

    @Override
    public void testFhirConversion() {
        logger.info("testFhirConversion service invoked.");

        String mrn = "C2S-DEV-3EKFHA";
        String mrnSystem = "https://bhits.github.io/consent2share/";

        XacmlRequestDto xacmlRequestDto = new XacmlRequestDto();
        xacmlRequestDto.setPatientId(new PatientIdDto(mrnSystem, mrn));
        xacmlRequestDto.setPurposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT);
        xacmlRequestDto.setMessageId("testmessageid");
        xacmlRequestDto.setRecipientNpi("1003220674");
        xacmlRequestDto.setIntermediaryNpi("1013900992");

        policyProvider.getPolicies(xacmlRequestDto);
    }

    @Override
    public ConsentBundleAndPatientDto tempGetFhirConsent(String mrn) {
        logger.info("tempGetFhirConsent service invoked. MRN = " + mrn);
        return policyProvider.tempGetFhirConsent(mrn);
    }
}
