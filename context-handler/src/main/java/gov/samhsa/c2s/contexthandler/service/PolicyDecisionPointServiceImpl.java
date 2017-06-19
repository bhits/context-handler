package gov.samhsa.c2s.contexthandler.service;

import ch.qos.logback.audit.AuditException;
import gov.samhsa.c2s.common.audit.AuditClient;
import gov.samhsa.c2s.common.audit.PredicateKey;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorException;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverterException;
import gov.samhsa.c2s.contexthandler.service.audit.ContextHandlerAuditVerb;
import gov.samhsa.c2s.contexthandler.service.audit.ContextHandlerPredicateKey;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlResponseDto;
import gov.samhsa.c2s.contexthandler.service.exception.C2SAuditException;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;
import gov.samhsa.c2s.contexthandler.service.util.RequestGenerator;
import lombok.extern.slf4j.Slf4j;
import org.herasaf.xacml.core.WritingException;
import org.herasaf.xacml.core.api.PDP;
import org.herasaf.xacml.core.api.PolicyRepository;
import org.herasaf.xacml.core.api.PolicyRetrievalPoint;
import org.herasaf.xacml.core.api.UnorderedPolicyRepository;
import org.herasaf.xacml.core.context.RequestMarshaller;
import org.herasaf.xacml.core.context.impl.RequestType;
import org.herasaf.xacml.core.context.impl.ResponseType;
import org.herasaf.xacml.core.context.impl.ResultType;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.herasaf.xacml.core.policy.PolicyMarshaller;
import org.herasaf.xacml.core.policy.impl.AttributeAssignmentType;
import org.herasaf.xacml.core.policy.impl.ObligationType;
import org.herasaf.xacml.core.simplePDP.SimplePDPFactory;
import org.herasaf.xacml.core.simplePDP.initializers.InitializerExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * ss PolicyDecisionPointServiceImpl.
 */
@Service
@Slf4j
public class PolicyDecisionPointServiceImpl implements PolicyDecisionPointService {

    /**
     * The policy provider.
     */
    @Autowired
    private PolicyProvider policyProvider;

    /**
     * The request generator.
     */
    @Autowired
    private RequestGenerator requestGenerator;

    @Autowired
    private Optional<AuditClient> auditClient;

    /**
     * The document accessor.
     */
    @Autowired
    private DocumentAccessor documentAccessor;

    /**
     * The document xml converter.
     */
    @Autowired
    private DocumentXmlConverter documentXmlConverter;

    @Override
    public XacmlResponseDto evaluateRequest(XacmlRequestDto xacmlRequest)
            throws C2SAuditException, NoPolicyFoundException,
            PolicyProviderException {
        log.info("evaluateRequest invoked");

        final RequestType request = requestGenerator.generateRequest(xacmlRequest);
        log.debug(createPDPRequestLogMessage(request));

        return managePoliciesAndEvaluateRequest(request, xacmlRequest);
    }

    @PostConstruct
    public void afterPropertiesSet() {
        // initialize herasaf
        InitializerExecutor.runInitializers();
    }

    private synchronized XacmlResponseDto managePoliciesAndEvaluateRequest(
            RequestType request, XacmlRequestDto xacmlRequest)
            throws C2SAuditException, NoPolicyFoundException,
            PolicyProviderException {
        PDP pdp = getSimplePDP();
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
        List<String> pdpObligations = new ArrayList<>();
        final XacmlResponseDto xacmlResponse = XacmlResponseDto.builder().pdpDecision("DENY").pdpObligations
                (pdpObligations).build();

        final ResponseType response = simplePDP.evaluate(request);
        for (final ResultType r : response.getResults()) {
            log.debug("PDP Decision: " + r.getDecision().toString());
            xacmlResponse.setPdpDecision(r.getDecision().toString());

            if (r.getObligations() != null) {
                final List<String> obligations = new LinkedList<>();
                for (final ObligationType o : r.getObligations()
                        .getObligations()) {
                    for (final AttributeAssignmentType a : o
                            .getAttributeAssignments()) {
                        for (final Object c : a.getContent()) {
                            log.debug("With Obligation: " + c);
                            obligations.add(c.toString());
                        }
                    }
                }
                xacmlResponse.setPdpObligations(obligations);
            }
        }

        log.debug("xacmlResponse.pdpDecision: "
                + xacmlResponse.getPdpDecision());
        log.debug("xacmlResponse is ready!");
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
            log.error(e.getMessage(), e);
            undeployAllPolicies(pdp);
            throw new C2SAuditException(e.getMessage(), e);
        }
    }

    private void undeployAllPolicies(PDP pdp) {
        final PolicyRepository repo = (PolicyRepository) pdp
                .getPolicyRepository();
        final List<Evaluatable> policies = new LinkedList<>(
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

        Map<PredicateKey, String> predicateMap = null;

        if (auditClient.isPresent()) {
            predicateMap = auditClient.get()
                    .createPredicateMap();
            final String policyString = writer.toString();
            writer.close();
            final NodeList policyIdNodeList = documentAccessor.getNodeList(
                    documentXmlConverter.loadDocument(policyString), "//@PolicyId");
            Set<String> policyIdSet = null;
            if (policyIdNodeList.getLength() > 0) {
                policyIdSet = new HashSet<>();
                for (int i = 0; i < policyIdNodeList.getLength(); i++) {
                    policyIdSet.add(policyIdNodeList.item(i).getNodeValue());
                }
            }
            predicateMap.put(ContextHandlerPredicateKey.XACML_POLICY, policyString);
            if (policyIdSet != null) {
                predicateMap.put(ContextHandlerPredicateKey.XACML_POLICY_ID, policyIdSet.toString());
            }
            auditClient.get().audit(this, xacmlRequest.getMessageId(), ContextHandlerAuditVerb.DEPLOY_POLICY,
                    xacmlRequest.getPatientId().getExtension(), predicateMap);
        }
    }

    private String createPDPRequestLogMessage(RequestType request) {
        final String logMsgPrefix = "PDP Request: ";
        final String errMsg = "Failed during marshalling PDP Request";
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            RequestMarshaller.marshal(request, baos);
            return new StringBuilder().append(logMsgPrefix).append(new String(baos.toByteArray())).toString();
        } catch (Exception e) {
            log.error(new StringBuilder().append(errMsg).append(" : ").append(e.getMessage()).toString());
        }
        return logMsgPrefix + errMsg;
    }

    private PDP getSimplePDP() {
        return SimplePDPFactory.getSimplePDP();
    }
}
