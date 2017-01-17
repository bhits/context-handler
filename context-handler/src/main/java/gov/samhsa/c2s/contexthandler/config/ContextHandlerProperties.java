package gov.samhsa.c2s.contexthandler.config;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "c2s.context-handler")
@Data
public class ContextHandlerProperties {

    @NotNull
    @Valid
    private Pid pid;

    @NotNull
    @Valid
    private PdpRequest pdpRequest;

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

        @NotNull
        @Valid
        private Action action;

        @Data
        public static class Resource {
            @NotEmpty
            private String typeCode;
            @NotEmpty
            private String status;
        }

        @Data
        public static class Action {
            @NotEmpty
            private String actionId;
        }
    }
}