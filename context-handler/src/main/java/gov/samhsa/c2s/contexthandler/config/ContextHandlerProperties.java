package gov.samhsa.c2s.contexthandler.config;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "c2s.context-handler")
@Data
@Validated
public class ContextHandlerProperties {

    @NotNull
    @Valid
    private Pid pid;

    @NotNull
    @Valid
    private PdpRequest pdpRequest;

    @NotBlank
    private String policyProvider;

    @Data
    public static class Pid {
        @NotEmpty
        private String type;
    }

    @Data
    public static class PdpRequest {
        @NotNull
        @Valid
        private Resource resource;

        @Data
        public static class Resource {
            @NotEmpty
            private String typeCode;
            @NotEmpty
            private String status;
        }
    }
}