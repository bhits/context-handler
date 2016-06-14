package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlResponseDto;
import gov.samhsa.c2s.contexthandler.service.exception.C2SAuditException;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            logger.debug("PDP Decision: " + xacmlResponse.getPdpDecision());
            logger.debug( "PDP Obligations: ", xacmlResponse.getPdpObligations().toString());
            return xacmlResponse;
        } catch (final NoPolicyFoundException e) {
            final XacmlResponseDto xacmlResponse = new XacmlResponseDto();
            xacmlResponse.setPdpDecision(e.getClass().getName());
            return xacmlResponse;
        }
    }
}
