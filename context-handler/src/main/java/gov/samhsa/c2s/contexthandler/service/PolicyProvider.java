package gov.samhsa.c2s.contexthandler.service;


import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;
import org.herasaf.xacml.core.policy.Evaluatable;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * The Interface PolicyProvider.
 */
public interface PolicyProvider {

    /**
     * Gets the policies.
     *
     * @param xacmlRequest the xacml request
     * @return the policies
     * @throws NoPolicyFoundException  the no policy found exception
     * @throws PolicyProviderException the policy provider exception
     */
    List<Evaluatable> getPolicies(XacmlRequestDto xacmlRequest) throws NoPolicyFoundException, PolicyProviderException;

    @PostConstruct
    default void afterPropertiesSet() {
        LoggerFactory.getLogger(PolicyProvider.class).info("Loaded Policy Provider: " + this.getClass());
    }
}
