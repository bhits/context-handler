package gov.samhsa.c2s.contexthandler.config;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "c2s.context-handler.fhir")
@Data
public class FhirProperties {
    @NotNull
    private boolean enabled;

    @NotEmpty
    private String serverUrl;

    @NotEmpty
    private String clientSocketTimeoutInMs;
}
