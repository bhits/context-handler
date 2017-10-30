package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlResponseDto;
import gov.samhsa.c2s.contexthandler.service.exception.C2SAuditException;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Optional;
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
    private Tracer tracer;

    @Override
    public XacmlResponseDto enforcePolicy(XacmlRequestDto xacmlRequest) throws C2SAuditException {
        try {
            logger.debug("policyDesicionPoint.evaluateRequest(xacmlRequest) is invoked");
            final String messageId = Optional.ofNullable(tracer)
                    .map(Tracer::getCurrentSpan)
                    .map(Span::traceIdString)
                    .filter(StringUtils::hasText)
                    .orElseGet(() -> Optional.of(xacmlRequest)
                            .map(XacmlRequestDto::getMessageId)
                            .map(String::trim)
                            .filter(StringUtils::hasText)
                            .orElseGet(() -> UUID.randomUUID().toString()));
            xacmlRequest.setMessageId(messageId);
            final XacmlResponseDto xacmlResponse = policyDesicionPointService
                    .evaluateRequest(xacmlRequest);
            logger.debug(() -> "PDP Decision: " + xacmlResponse.getPdpDecision());
            logger.debug(() -> "PDP Obligations: " + xacmlResponse.getPdpObligations().toString());
            return xacmlResponse;
        } catch (final NoPolicyFoundException e) {
            final XacmlResponseDto xacmlResponse = XacmlResponseDto.builder()
                    .pdpDecision(PolicyDecisionPointServiceImpl.DENY)
                    .pdpObligations(Collections.emptyList())
                    .build();
            return xacmlResponse;
        }
    }
}
