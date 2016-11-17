package gov.samhsa.c2s.contexthandler.config;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Min;

@Component
@ConditionalOnProperty(name = "c2s.context-handler.audit-client.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "c2s.context-handler.audit-client")
@Data
public class AuditClientProperties {
    @NotEmpty
    private String host;
    @Min(1)
    private int port;
}
