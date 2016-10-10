package gov.samhsa.c2s.contexthandler.service.util;


import gov.samhsa.c2s.contexthandler.service.dto.PolicyContentDto;
import gov.samhsa.c2s.contexthandler.service.dto.PolicyDto;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyIdNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Optional;

import static gov.samhsa.c2s.contexthandler.service.util.DOMUtils.bytesToDocument;
import static gov.samhsa.c2s.contexthandler.service.xacml.XACMLXPath.XPATH_POLICY_ID;
import static gov.samhsa.c2s.contexthandler.service.xacml.XACMLXPath.XPATH_POLICY_SET_ID;


public class MappingUtils {

    public static PolicyDto toPolicyDto(byte[] policy) {
        String id = null;
        final Document policyDoc = bytesToDocument(policy);
        final Optional<Node> policySetId = DOMUtils.getNode(policyDoc,
                XPATH_POLICY_SET_ID);
        if (policySetId.isPresent()) {
            id = policySetId.get().getNodeValue();
        } else {
            final Optional<Node> policyId = DOMUtils.getNode(policyDoc,
                    XPATH_POLICY_ID);
            if (policyId.isPresent()) {
                id = policyId.get().getNodeValue();
            }
        }
        if (id == null) {
            throw new PolicyIdNotFoundException(
                    "Cannot find the PolicyId/PolicySetId in one or more of the submitted policies!");
        } else {
            final PolicyDto dto = new PolicyDto();
            dto.setId(id);
            dto.setPolicy(policy);
            return dto;
        }
    }

    public static PolicyDto toPolicyDto(PolicyContentDto policyContentDto) {
        return toPolicyDto(policyContentDto.getPolicy());
    }
}
