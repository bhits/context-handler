package gov.samhsa.c2s.contexthandler.infrastructure;

import gov.samhsa.c2s.common.consentgen.ConsentGenException;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@FeignClient(name = "pcm")
@Service
public interface PcmService {

    @RequestMapping(value = "/consents/export/xacml", method = RequestMethod.POST)
    public Object exportXACMLConsent(@Valid @RequestBody XacmlRequestDto xacmlRequestDto) ;
}