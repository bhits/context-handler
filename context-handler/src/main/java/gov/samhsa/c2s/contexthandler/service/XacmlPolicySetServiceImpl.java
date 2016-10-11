package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.contexthandler.service.dto.PolicyContainerDto;
import gov.samhsa.c2s.contexthandler.service.dto.PolicyDto;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;
import gov.samhsa.c2s.contexthandler.service.util.DOMUtils;
import gov.samhsa.c2s.contexthandler.service.util.PolicyValidationUtils;
import org.apache.commons.io.IOUtils;
import org.herasaf.xacml.core.SyntaxException;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.herasaf.xacml.core.policy.PolicyMarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import static gov.samhsa.c2s.contexthandler.service.xacml.XACMLXPath.XPATH_POLICY_SET_ID;
import static gov.samhsa.c2s.contexthandler.service.xacml.XACMLXPath.XPATH_POLICY_SET_POLICY_COMBINING_ALG_ID;

@Service
public class XacmlPolicySetServiceImpl implements XacmlPolicySetService {
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String DEFAULT_WILDCARD = "*";
    private static final String POLICY_SET_XML_TEMPLATE_FILE_NAME = "PolicySetTemplate.xml";
    /**
     * The Constant DELIMITER_AMPERSAND.
     */
    private static final String DELIMITER_AMPERSAND = "&";
    /**
     * The Constant DELIMITER_COLON.
     */
    private static final String DELIMITER_COLON = ":";
    private static final String PARAM_NAME_WILDCARD = "wildcard";
    private static final String PARAM_NAME_FORCE = "force";
    private static final String PARAM_NAME_POLICY_SET_ID = "policySetId";
    private static final String PARAM_NAME_POLICY_COMBINING_ALG_ID = "policyCombiningAlgId";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PolicyCombiningAlgIdValidator policyCombiningAlgIdValidator;
    /**
     * The pid domain type.
     */
    @Value("${c2s.context-handler.pid.type}")
    private String pidDomainType;

    @Override
    public Evaluatable getPoliciesCombinedAsPolicySet(PolicyContainerDto policies, String policySetId, String policyCombiningAlgId) throws NoPolicyFoundException, PolicyProviderException {
        try {
            // Validate policyCombiningAlgId
            policyCombiningAlgId = policyCombiningAlgIdValidator
                    .validateAndReturn(policyCombiningAlgId);


            // Construct a policy set template
            final Document policySet = initPolicySetTemplate();

            // Set policySetId and policyCombiningAlgId
            if (!StringUtils.hasText(policySetId)) {
                policySetId = UUID.randomUUID().toString();
            }
            DOMUtils.getNode(policySet, XPATH_POLICY_SET_ID).get()
                    .setNodeValue(policySetId);
            DOMUtils.getNode(policySet, XPATH_POLICY_SET_POLICY_COMBINING_ALG_ID)
                    .get().setNodeValue(policyCombiningAlgId);

            // Append all policies to the policy set template
            policies.getPolicies()
                    .stream()
                    .map(PolicyDto::getPolicy)
                    .map(DOMUtils::bytesToDocument)
                    .map(Document::getDocumentElement)
                    .map(policy -> policy.cloneNode(true))
                    .map(clonedPolicy -> policySet.importNode(clonedPolicy, true))
                    .forEach(
                            importedPolicy -> policySet.getDocumentElement()
                                    .appendChild(importedPolicy));

            // Construct and return the response
            final PolicyDto response = new PolicyDto();
            response.setId(policySetId);
            response.setPolicy(DOMUtils.documentToBytes(policySet));
            response.setValid(PolicyValidationUtils.validate(response.getPolicy()));
            logger.debug(new String(response.getPolicy()));
            Evaluatable evaluatable = PolicyMarshaller
                    .unmarshal(new ByteArrayInputStream(response.getPolicy()));
            return evaluatable;
        } catch (final SyntaxException e) {
            logger.error(e.getMessage(), e);
            throw new PolicyProviderException(e.getMessage(), e);
        } catch (final HttpStatusCodeException e) {
            logger.error(e.getMessage(), e);
            if (e.getStatusCode().is4xxClientError()) {
                logger.info(e.getMessage());
                throw new NoPolicyFoundException(e.getMessage(), e);
            } else {
                throw new PolicyProviderException(e.getMessage(), e);
            }
        }
    }

    private Document initPolicySetTemplate() {
        byte[] policySetTemplateBytes = null;
        try {
            policySetTemplateBytes = IOUtils.toByteArray(getClass()
                    .getClassLoader().getResourceAsStream(
                            POLICY_SET_XML_TEMPLATE_FILE_NAME));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return DOMUtils.bytesToDocument(policySetTemplateBytes);
    }
}
