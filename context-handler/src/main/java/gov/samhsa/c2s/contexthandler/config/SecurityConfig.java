package gov.samhsa.c2s.contexthandler.config;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

import static gov.samhsa.c2s.common.oauth2.OAuth2ScopeUtils.hasScope;

@Configuration
public class SecurityConfig {

    private static final String RESOURCE_ID = "contextHandler";

    @Bean
    public ResourceServerConfigurer resourceServer(SecurityProperties securityProperties) {
        return new ResourceServerConfigurerAdapter() {
            @Override
            public void configure(ResourceServerSecurityConfigurer resources) {
                resources.resourceId(RESOURCE_ID);
            }

            @Override
            public void configure(HttpSecurity http) throws Exception {
                if (securityProperties.isRequireSsl()) {
                    http.requiresChannel().anyRequest().requiresSecure();
                }
                http.authorizeRequests()
                        // TODO: May add permission for accessing following resource
                        .antMatchers(HttpMethod.POST, "/policyEnforcement/**").permitAll()
                        // FIXME: !!!REMOVE THE FOLLOWING LINE BEFORE COMMITING!!!
                        .antMatchers(HttpMethod.GET, "/tempGetMockedFhirConsent").permitAll()
                        .antMatchers(HttpMethod.GET, "/tempGetFhirConsent/**").permitAll()
                        // Security scope for accessing management endpoint
                        .antMatchers(HttpMethod.GET, "/management/**").access(hasScope("contextHandler.management"))
                        .antMatchers(HttpMethod.POST, "/management/**").access(hasScope("contextHandler.management"))
                        .anyRequest().denyAll();
            }
        };
    }
}