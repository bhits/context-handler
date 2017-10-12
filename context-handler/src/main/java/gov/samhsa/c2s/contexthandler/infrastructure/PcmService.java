package gov.samhsa.c2s.contexthandler.infrastructure;

import gov.samhsa.c2s.contexthandler.service.dto.ConsentXacmlDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

@FeignClient(name = "pcm")
public interface PcmService {

    @RequestMapping(value = "/consents/export/xacml", method = RequestMethod.POST)
    ConsentXacmlDto exportXACMLConsent(@Valid @RequestBody XacmlRequestDto xacmlRequestDto);
}