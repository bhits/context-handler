package gov.samhsa.c2s.contexthandler.infrastructure.dto;

import lombok.Data;

@Data
public class MrnDto {
    private String codeSystem;
    private String codeSystemOID;
    private String displayName;
    private String prefix;
}