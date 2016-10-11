package gov.samhsa.c2s.contexthandler.service.util;

import gov.samhsa.c2s.contexthandler.service.dto.PolicyDto;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyIdNotFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyNotFoundException;
import org.springframework.util.StringUtils;

import java.util.List;


public class AssertionUtils {

    public static void assertPoliciesNotEmpty(final List<PolicyDto> policies,
                                              final String policyId) {
        if (policies == null || policies.size() == 0) {
            final StringBuilder errorBuilder = new StringBuilder();
            errorBuilder.append(
                    "Policy/PolicySet cannot be found with given ID: ").append(
                    policyId);
            throw new PolicyNotFoundException(errorBuilder.toString());
        }
    }

    public static void assertPolicyId(String policyId) {
        if (!StringUtils.hasText(policyId)) {
            throw new PolicyIdNotFoundException("Policy ID must have a text!");
        }
    }


}
