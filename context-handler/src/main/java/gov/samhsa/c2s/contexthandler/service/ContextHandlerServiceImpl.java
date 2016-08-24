package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlResponseDto;
import gov.samhsa.c2s.contexthandler.service.exception.C2SAuditException;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ContextHandlerServiceImpl implements ContextHandlerService {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The policy desicion point. */
    @Autowired
    private PolicyDecisionPointService policyDesicionPointService;

    @Override
    public XacmlResponseDto enforcePolicy(XacmlRequestDto xacmlRequest) throws C2SAuditException{
        try {
            logger.debug("policyDesicionPoint.evaluateRequest(xacmlRequest) is invoked");

            xacmlRequest.setMessageId(UUID.randomUUID().toString());
            final XacmlResponseDto xacmlResponse = policyDesicionPointService
                    .evaluateRequest(xacmlRequest);
            logger.debug(() -> "PDP Decision: " + xacmlResponse.getPdpDecision());
            logger.debug( () -> "PDP Obligations: " + xacmlResponse.getPdpObligations().toString());
            return xacmlResponse;
        } catch (final NoPolicyFoundException e) {
            List<String> pdpObligations = new ArrayList<String>();
            final XacmlResponseDto xacmlResponse = XacmlResponseDto.builder().pdpDecision("DENY").pdpObligations(pdpObligations).build();
            xacmlResponse.setPdpDecision(e.getClass().getName());
            return xacmlResponse;
        }
    }
}
