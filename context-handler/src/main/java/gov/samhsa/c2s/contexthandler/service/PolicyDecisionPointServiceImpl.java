/*******************************************************************************
 * Open Behavioral Health Information Technology Architecture (OBHITA.org)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
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

import ch.qos.logback.audit.AuditException;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlResponseDto;
import gov.samhsa.c2s.contexthandler.service.exception.C2SAuditException;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;
import gov.samhsa.c2s.contexthandler.service.audit.ContextHandlerAuditVerb;
import gov.samhsa.c2s.contexthandler.service.audit.ContextHandlerPredicateKey;
import gov.samhsa.c2s.contexthandler.service.util.RequestGenerator;
import gov.samhsa.mhc.common.audit.AuditService;
import gov.samhsa.mhc.common.audit.PredicateKey;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessorException;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverter;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverterException;
import lombok.val;
import org.herasaf.xacml.core.WritingException;
import org.herasaf.xacml.core.api.PDP;
import org.herasaf.xacml.core.api.PolicyRepository;
import org.herasaf.xacml.core.api.PolicyRetrievalPoint;
import org.herasaf.xacml.core.api.UnorderedPolicyRepository;
import org.herasaf.xacml.core.context.impl.RequestType;
import org.herasaf.xacml.core.context.impl.ResponseType;
import org.herasaf.xacml.core.context.impl.ResultType;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.herasaf.xacml.core.policy.PolicyMarshaller;
import org.herasaf.xacml.core.policy.impl.AttributeAssignmentType;
import org.herasaf.xacml.core.policy.impl.ObligationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static org.herasaf.xacml.core.simplePDP.SimplePDPFactory.getSimplePDP;

/**
 * ss PolicyDecisionPointServiceImpl.
 */
@Service
public class PolicyDecisionPointServiceImpl implements PolicyDecisionPointService {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/** The policy provider. */
	@Autowired
	private PolicyProvider policyProvider;

	/** The request generator. */
	@Autowired
	private RequestGenerator requestGenerator;

	@Autowired
	private AuditService auditService;

	/** The document accessor. */
	@Autowired
	private DocumentAccessor documentAccessor;

	/** The document xml converter. */
	@Autowired
	private DocumentXmlConverter documentXmlConverter;

	@Override
	public XacmlResponseDto evaluateRequest(XacmlRequestDto xacmlRequest)
			throws C2SAuditException, NoPolicyFoundException,
			PolicyProviderException {
		logger.info("evaluateRequest invoked");

		final RequestType request = requestGenerator.generateRequest(xacmlRequest);

		return managePoliciesAndEvaluateRequest(request, xacmlRequest);
	}

	private synchronized XacmlResponseDto managePoliciesAndEvaluateRequest(
			RequestType request, XacmlRequestDto xacmlRequest)
			throws C2SAuditException, NoPolicyFoundException,
			PolicyProviderException {
		final PDP pdp = getSimplePDP();
		deployPolicies(pdp, xacmlRequest);
		return managePoliciesAndEvaluateRequest(pdp, request);
	}

	private List<Evaluatable> deployPolicies(PDP pdp, XacmlRequestDto xacmlRequest)
			throws C2SAuditException, NoPolicyFoundException,
			PolicyProviderException {
		final List<Evaluatable> deployedPolicies = getPolicies(xacmlRequest);
		deployPolicies(pdp, deployedPolicies, xacmlRequest, true);
		return deployedPolicies;
	}

	List<Evaluatable> getPolicies(XacmlRequestDto xacmlRequest)
			throws NoPolicyFoundException, PolicyProviderException {

		return policyProvider.getPolicies(xacmlRequest);
	}


	private XacmlResponseDto managePoliciesAndEvaluateRequest(PDP pdp,
														   RequestType request) {
		final XacmlResponseDto xacmlResponse = evaluateRequest(pdp, request);
		undeployAllPolicies(pdp);
		return xacmlResponse;
	}

	private XacmlResponseDto evaluateRequest(PDP simplePDP, RequestType request) {
		//final XacmlResponseDto xacmlResponse = new XacmlResponseDto();
		List<String> pdpObligations = new ArrayList<String>();
		val xacmlResponse = XacmlResponseDto.builder().pdpDecision("DENY").pdpObligations(pdpObligations).build();

		final ResponseType response = simplePDP.evaluate(request);
		for (final ResultType r : response.getResults()) {
			logger.debug( "PDP Decision: " + r.getDecision().toString());
			xacmlResponse.setPdpDecision(r.getDecision().toString());

			if (r.getObligations() != null) {
				final List<String> obligations = new LinkedList<String>();
				for (final ObligationType o : r.getObligations()
						.getObligations()) {
					for (final AttributeAssignmentType a : o
							.getAttributeAssignments()) {
						for (final Object c : a.getContent()) {
							logger.debug( "With Obligation: " + c);
							obligations.add(c.toString());
						}
					}
				}
				xacmlResponse.setPdpObligations(obligations);
			}
		}

		logger.debug( "xacmlResponse.pdpDecision: "
				+ xacmlResponse.getPdpDecision());
		logger.debug("xacmlResponse is ready!");
		return xacmlResponse;
	}


	void deployPolicies(PDP pdp, List<Evaluatable> policies,
						XacmlRequestDto xacmlRequest, boolean isAudited)
			throws C2SAuditException {
		try {
			final PolicyRetrievalPoint repo = pdp.getPolicyRepository();
			final UnorderedPolicyRepository repository = (UnorderedPolicyRepository) repo;
			repository.deploy(policies);
			if (isAudited) {
				for (final Evaluatable policy : policies) {
					auditPolicy(policy, xacmlRequest);
				}
			}
		} catch (AuditException | WritingException | IOException
				| DocumentAccessorException | DocumentXmlConverterException e) {
			logger.error(e.getMessage(), e);
			undeployAllPolicies(pdp);
			throw new C2SAuditException(e.getMessage(), e);
		}
	}

	private void undeployAllPolicies(PDP pdp) {
		final PolicyRepository repo = (PolicyRepository) pdp
				.getPolicyRepository();
		final List<Evaluatable> policies = new LinkedList<Evaluatable>(
				repo.getDeployment());
		for (final Evaluatable policy : policies) {
			repo.undeploy(policy.getId());
		}
	}

	private void auditPolicy(Evaluatable policy, XacmlRequestDto xacmlRequest)
			throws WritingException, IOException, DocumentAccessorException,
			AuditException {
		final StringWriter writer = new StringWriter();
		PolicyMarshaller.marshal(policy, writer);
		final Map<PredicateKey, String> predicateMap = auditService
				.createPredicateMap();
		final String policyString = writer.toString();
		writer.close();
		final NodeList policyIdNodeList = documentAccessor.getNodeList(
				documentXmlConverter.loadDocument(policyString), "//@PolicyId");
		Set<String> policyIdSet = null;
		if (policyIdNodeList.getLength() > 0) {
			policyIdSet = new HashSet<String>();
			for (int i = 0; i < policyIdNodeList.getLength(); i++) {
				policyIdSet.add(policyIdNodeList.item(i).getNodeValue());
			}
		}
		predicateMap.put(ContextHandlerPredicateKey.XACML_POLICY, policyString);
		if (policyIdSet != null) {
			predicateMap.put(ContextHandlerPredicateKey.XACML_POLICY_ID, policyIdSet.toString());
		}
		auditService.audit(this, xacmlRequest.getMessageId(), ContextHandlerAuditVerb.DEPLOY_POLICY,
				xacmlRequest.getPatientId().getExtension(), predicateMap);
	}

}
