package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.contexthandler.service.dto.PolicyContainerDto;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;
import org.herasaf.xacml.core.policy.Evaluatable;

/**
 * Created by sadhana.chandra on 6/24/2016.
 */
public interface XacmlPolicySetService {
    public Evaluatable getPoliciesCombinedAsPolicySet(PolicyContainerDto policies, String policySetId, String policyCombiningAlgId) throws NoPolicyFoundException, PolicyProviderException;
}
