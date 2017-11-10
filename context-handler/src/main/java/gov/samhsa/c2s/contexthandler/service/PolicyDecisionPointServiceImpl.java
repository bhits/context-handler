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
import gov.samhsa.c2s.contexthandler.service.dto.ObligationDto;
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
import org.herasaf.xacml.core.context.impl.DecisionType;
import org.herasaf.xacml.core.context.impl.RequestType;
import org.herasaf.xacml.core.context.impl.ResponseType;
import org.herasaf.xacml.core.context.impl.ResultType;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.herasaf.xacml.core.policy.PolicyMarshaller;
import org.herasaf.xacml.core.policy.impl.AttributeAssignmentType;
import org.herasaf.xacml.core.policy.impl.ObligationsType;
import org.herasaf.xacml.core.simplePDP.SimplePDPFactory;
import org.herasaf.xacml.core.simplePDP.initializers.InitializerExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * ss PolicyDecisionPointServiceImpl.
 */
@Service
@Slf4j
public class PolicyDecisionPointServiceImpl implements PolicyDecisionPointService {

    public static final String PERMIT = DecisionType.PERMIT.toString();
    public static final String DENY = DecisionType.DENY.toString();
    public static final String INDETERMINATE = DecisionType.INDETERMINATE.toString();
    public static final String NOT_APPLICABLE = DecisionType.NOT_APPLICABLE.toString();
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
    public XacmlResponseDto evaluateRequest(XacmlRequestDto xacmlRequest) {
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

    List<Evaluatable> getPolicies(XacmlRequestDto xacmlRequest) {

        return policyProvider.getPolicies(xacmlRequest);
    }

    private XacmlResponseDto managePoliciesAndEvaluateRequest(PDP pdp,
                                                              RequestType request) {
        final XacmlResponseDto xacmlResponse = evaluateRequest(pdp, request);
        undeployAllPolicies(pdp);
        return xacmlResponse;
    }

    private XacmlResponseDto evaluateRequest(PDP simplePDP, RequestType request) {
        final ResponseType response = simplePDP.evaluate(request);
        final XacmlResponseDto xacmlResponse = response.getResults().stream()
                .map(this::handleResult)
                .reduce(this::reduce)
                .orElseGet(() -> XacmlResponseDto.builder().pdpDecision(DENY).build());
        log.debug("xacmlResponse.pdpDecision: " + xacmlResponse.getPdpDecision());
        log.debug("xacmlResponse is ready!");
        return xacmlResponse;
    }

    private XacmlResponseDto handleResult(ResultType result) {
        final String decision = result.getDecision().toString();
        log.debug("PDP Decision: " + decision);
        final List<ObligationDto> obligations = Optional.ofNullable(result)
                .map(ResultType::getObligations)
                .map(ObligationsType::getObligations)
                .orElseGet(Collections::emptyList)
                .stream()
                .flatMap(obligation -> obligation.getAttributeAssignments().stream()
                        .map(AttributeAssignmentType::getContent)
                        .flatMap(List::stream)
                        .map(String.class::cast)
                        .peek(obligationValue -> log.debug("With Obligation: " + obligationValue))
                        .map(obligationValue -> ObligationDto.builder()
                                .obligationId(obligation.getObligationId())
                                .obligationValue(obligationValue).build()))
                .collect(toList());
        final XacmlResponseDto xacmlResponse = XacmlResponseDto.builder()
                .pdpObligations(obligations)
                .pdpDecision(decision).build();
        return xacmlResponse;
    }

    private XacmlResponseDto reduce(XacmlResponseDto x1, XacmlResponseDto x2) {
        final String decision = DENY.equalsIgnoreCase(x1.getPdpDecision()) || DENY.equalsIgnoreCase(x2.getPdpDecision()) ? DENY :
                INDETERMINATE.equalsIgnoreCase(x1.getPdpDecision()) || INDETERMINATE.equalsIgnoreCase(x2.getPdpDecision()) ? INDETERMINATE :
                        NOT_APPLICABLE.equalsIgnoreCase(x1.getPdpDecision()) || NOT_APPLICABLE.equalsIgnoreCase(x2.getPdpDecision()) ? NOT_APPLICABLE :
                                x2.getPdpDecision();
        final List<ObligationDto> obligations = Stream.concat(getObligationDtoStream(x1), getObligationDtoStream(x2))
                .collect(toList());
        return XacmlResponseDto.builder()
                .pdpDecision(decision)
                .pdpObligations(obligations)
                .build();
    }

    private Stream<ObligationDto> getObligationDtoStream(XacmlResponseDto xacmlResponseDto) {
        return Optional.of(xacmlResponseDto).map(XacmlResponseDto::getPdpObligations).orElseGet(Collections::emptyList).stream();
    }

    void deployPolicies(PDP pdp, List<Evaluatable> policies,
                        XacmlRequestDto xacmlRequest, boolean isAudited) {
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
                    xacmlRequest.getPatientIdentifier().getFullIdentifier(), predicateMap);
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
