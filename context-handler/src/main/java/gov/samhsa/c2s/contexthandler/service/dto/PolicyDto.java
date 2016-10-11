package gov.samhsa.c2s.contexthandler.service.dto;

import lombok.Data;

@Data
public class PolicyDto {

    private String id;
    private boolean valid;
    private byte[] policy;
}
