package gov.samhsa.c2s.contexthandler.infrastructure;

import gov.samhsa.c2s.contexthandler.infrastructure.dto.MrnDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "ums")
public interface UmsService {
    @RequestMapping(value = "/mrn", method = RequestMethod.GET)
    MrnDto getMrn();
}
