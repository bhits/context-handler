package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlResponseDto;
import gov.samhsa.c2s.contexthandler.service.exception.C2SAuditException;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;

/**
 * The Interface PolicyDecisionPointService.
 */
public interface PolicyDecisionPointService {

    XacmlResponseDto evaluateRequest(XacmlRequestDto xacmlRequest)
            throws C2SAuditException, NoPolicyFoundException,
            PolicyProviderException;
}
