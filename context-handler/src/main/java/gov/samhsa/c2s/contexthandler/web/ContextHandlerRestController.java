package gov.samhsa.c2s.contexthandler.web;

import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class ContextHandlerRestController {

/*    @RequestMapping(value = "/policyEnforcement", method = RequestMethod.POST)
    public XacmlResponseDto access(@Valid @RequestBody XacmlRequestDto xacmlRequest) {
        return new XacmlResponseDto();
    }*/
}
