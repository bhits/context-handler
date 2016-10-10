package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.contexthandler.service.exception.InvalidPolicyCombiningAlgIdException;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyCombiningAlgIdNotFoundException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


@Component
@ConfigurationProperties(prefix = "c2s.context-handler.polrep.policySet")
public class PolicyCombiningAlgIdValidatorImpl implements
        PolicyCombiningAlgIdValidator {

    private final Map<String, String> combiningAlgs = new HashMap<>();

    @Override
    public Map<String, String> getCombiningAlgs() {
        return combiningAlgs;
    }

    @Override
    public String validateAndReturn(String policyCombiningAlgId) {
        if (!StringUtils.hasText(policyCombiningAlgId)) {
            throw new PolicyCombiningAlgIdNotFoundException(
                    "policyCombiningAlgId must have a valid text!");
        }
        if (combiningAlgs.keySet().contains(policyCombiningAlgId)) {
            policyCombiningAlgId = combiningAlgs.get(policyCombiningAlgId);
        } else {
            if (combiningAlgs.entrySet().stream().map(Entry::getValue)
                    .noneMatch(policyCombiningAlgId::equals)) {
                final StringBuilder errorBuilder = new StringBuilder();
                errorBuilder.append("The policyCombiningAlgId: ")
                        .append(policyCombiningAlgId)
                        .append(" is not a valid value!");
                throw new InvalidPolicyCombiningAlgIdException(
                        errorBuilder.toString());
            }
        }
        return policyCombiningAlgId;
    }

}
