package gov.samhsa.c2s.contexthandler.service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PolicyContainerDto {

	private List<PolicyDto> policies;


}
