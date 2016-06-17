/*******************************************************************************
 * Open Behavioral Health Information Technology Architecture (OBHITA.org)
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.samhsa.c2s.contexthandler.service;


import gov.samhsa.c2s.contexthandler.service.dto.PolicyContainerDto;
import gov.samhsa.c2s.contexthandler.service.dto.PolicyDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;
import gov.samhsa.c2s.contexthandler.service.util.DOMUtils;
import gov.samhsa.c2s.contexthandler.service.util.PolicyCombiningAlgIds;
import gov.samhsa.c2s.contexthandler.service.util.PolicyValidationUtils;
import org.apache.commons.io.IOUtils;
import org.herasaf.xacml.core.SyntaxException;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.herasaf.xacml.core.policy.PolicyMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static gov.samhsa.c2s.contexthandler.service.util.AssertionUtils.assertPoliciesNotEmpty;
import static gov.samhsa.c2s.contexthandler.service.util.AssertionUtils.assertPolicyId;
import static gov.samhsa.c2s.contexthandler.service.xacml.XACMLXPath.XPATH_POLICY_SET_ID;
import static gov.samhsa.c2s.contexthandler.service.xacml.XACMLXPath.XPATH_POLICY_SET_POLICY_COMBINING_ALG_ID;

/**
 * The Class PolRepPolicyProvider.
 */
@Service
public class JdbcPolicyProviderImpl implements PolicyProvider {
    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The Constant PERCENTILE.
     */
    private static final String PERCENTILE = "%";

    private static final String POLICY_SET_XML_TEMPLATE_FILE_NAME = "PolicySetTemplate.xml";

    /**
     * The Constant DELIMITER_AMPERSAND.
     */
    private static final String DELIMITER_AMPERSAND = "&";

    /**
     * The Constant DELIMITER_COLON.
     */
    private static final String DELIMITER_COLON = ":";

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String DEFAULT_WILDCARD = "*";

    private static final String PARAM_NAME_WILDCARD = "wildcard";
    private static final String PARAM_NAME_FORCE = "force";
    private static final String PARAM_NAME_POLICY_SET_ID = "policySetId";
    private static final String PARAM_NAME_POLICY_COMBINING_ALG_ID = "policyCombiningAlgId";


    /**
     * The pid domain type.
     */
    @Value("${mhc.context-handler.pid.type}")
    private String pidDomainType;

    @Autowired
    PolicyCombiningAlgIdValidator policyCombiningAlgIdValidator;

    @Autowired
    PolicyGetterService policyGetterService;

    @Override
    public List<Evaluatable> getPolicies(XacmlRequestDto xacmlRequest)
            throws NoPolicyFoundException, PolicyProviderException {
        try {
            final String mrn = xacmlRequest.getPatientId().getExtension();
            final String mrnDomain = xacmlRequest.getPatientId().getRoot();

            final String policyId = toPolicyId(mrn, mrnDomain,
                    xacmlRequest.getRecipientNpi(),
                    xacmlRequest.getIntermediaryNpi());
            //PolicyDto policyDto = new PolicyDto();
            final PolicyDto policyDto = getPoliciesCombinedAsPolicySet(policyId, UUID
                            .randomUUID().toString(),
                    PolicyCombiningAlgIds.DENY_OVERRIDES.getUrn());
            final Evaluatable policySet = PolicyMarshaller
                    .unmarshal(new ByteArrayInputStream(policyDto.getPolicy()));


            return Arrays.asList(policySet);
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

    private String toPolicyId(String pid, String pidDomain,
                              String recipientSubjectNPI, String intermediarySubjectNPI) {
        final StringBuilder policyIdBuilder = new StringBuilder();
        policyIdBuilder.append(pid);
        policyIdBuilder.append(DELIMITER_COLON);
        policyIdBuilder.append(DELIMITER_AMPERSAND);
        policyIdBuilder.append(pidDomain);
        policyIdBuilder.append(DELIMITER_AMPERSAND);
        policyIdBuilder.append(pidDomainType);
        policyIdBuilder.append(DELIMITER_COLON);
        policyIdBuilder.append(recipientSubjectNPI);
        policyIdBuilder.append(DELIMITER_COLON);
        policyIdBuilder.append(intermediarySubjectNPI);
        policyIdBuilder.append(DELIMITER_COLON);
        policyIdBuilder.append(PERCENTILE);
        return policyIdBuilder.toString();
    }


    public PolicyDto getPoliciesCombinedAsPolicySet(String policyId, String policySetId, String policyCombiningAlgId) {

        // Validate policyCombiningAlgId
        policyCombiningAlgId = policyCombiningAlgIdValidator
                .validateAndReturn(policyCombiningAlgId);

        // Get all policies from db
        final PolicyContainerDto policies = getPolicies(policyId);

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
        return response;
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

    public PolicyContainerDto getPolicies(String policyId) {

        // Assert policy id
        assertPolicyId(policyId);

        // get policies from pcm database
        List<PolicyDto> policies = policyGetterService.getPolicies(policyId);

        // Assert that at least one policy is found
        assertPoliciesNotEmpty(policies, policyId);

        return PolicyContainerDto.builder().policies(policies).build();

    }

}
