package gov.samhsa.c2s.contexthandler.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

    private Ssn ssn;
    private Npi npi;
    private Pou pou;
    private Mrn mrn;

    @Data
    public static class Identifier {
        @NotNull
        private String system;

        @NotEmpty
        private String oid;

        @NotEmpty
        private String label;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Mrn extends Identifier{ }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Ssn extends Identifier{ }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Npi extends Identifier{ }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Pou extends Identifier{ }
}
