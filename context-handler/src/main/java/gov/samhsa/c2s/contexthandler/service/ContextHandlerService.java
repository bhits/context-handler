package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlResponseDto;
import gov.samhsa.c2s.contexthandler.service.exception.C2SAuditException;

public interface ContextHandlerService {

    XacmlResponseDto enforcePolicy(XacmlRequestDto xacmlRequest) throws C2SAuditException;

}
