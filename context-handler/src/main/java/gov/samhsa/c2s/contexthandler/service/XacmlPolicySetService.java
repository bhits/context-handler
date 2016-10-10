package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.contexthandler.service.dto.PolicyContainerDto;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;
import org.herasaf.xacml.core.policy.Evaluatable;

public interface XacmlPolicySetService {
    Evaluatable getPoliciesCombinedAsPolicySet(PolicyContainerDto policies, String policySetId, String policyCombiningAlgId) throws NoPolicyFoundException, PolicyProviderException;
}
