package gov.samhsa.c2s.contexthandler.web;

import gov.samhsa.c2s.contexthandler.service.ContextHandlerService;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlResponseDto;
import gov.samhsa.c2s.contexthandler.service.exception.C2SAuditException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class ContextHandlerRestController {

    @Autowired
    private ContextHandlerService contextHandlerService;

    @RequestMapping("/")
    public String index() {
        return "Welcome to Context Handler Service";
    }

    @RequestMapping(value = "/policyEnforcement", method = RequestMethod.POST)
    public XacmlResponseDto access(@Valid @RequestBody XacmlRequestDto xacmlRequest) throws C2SAuditException {
        return contextHandlerService.enforcePolicy(xacmlRequest);
    }

    // FIXME: !!!REMOVE THE FOLLOWING METHOD BEFORE COMMITING!!!
    @RequestMapping(value = "/tempGetMockedFhirConsent", method = RequestMethod.GET)
    public void tempGetMockedFhirConsent() throws C2SAuditException {
        contextHandlerService.testFhirConversion();
    }

    //FIXME: !!!REMOVE THE FOLLOWING BEFORE MERGING TO MASTER!!!
    //Given a MRN, get bundle from FHIR server
    @RequestMapping(value = "/tempGetFhirConsent/{mrn}/{system}", method = RequestMethod.GET)
    public void tempGetFhirConsent(@PathVariable String mrn,
                                   @PathVariable String system) throws C2SAuditException {
        contextHandlerService.tempGetFhirConsent(mrn, system);
    }
}
