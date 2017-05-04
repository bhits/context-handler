package gov.samhsa.c2s.contexthandler.service;


import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.contexthandler.config.ContextHandlerProperties;
import gov.samhsa.c2s.contexthandler.config.FhirProperties;
import gov.samhsa.c2s.contexthandler.service.dto.ConsentBundleAndPatientDto;
import gov.samhsa.c2s.contexthandler.service.dto.PolicyContainerDto;
import gov.samhsa.c2s.contexthandler.service.dto.PolicyDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;
import gov.samhsa.c2s.contexthandler.service.util.PolicyCombiningAlgIds;
import gov.samhsa.c2s.contexthandler.service.util.PolicyDtoRowMapper;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.hl7.fhir.dstu3.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static gov.samhsa.c2s.contexthandler.service.util.AssertionUtils.assertPoliciesNotEmpty;
import static gov.samhsa.c2s.contexthandler.service.util.AssertionUtils.assertPolicyId;

@Service
@ConditionalOnMissingBean(FhirProperties.class)
public class JdbcPolicyProviderImpl implements PolicyProvider {

    public static final String SQL_GET_XACML_CONSENT_WC = "select consent.consent_reference_id, consent.xacml_ccd  "
            + " from consent "
            + " where consent.consent_reference_id like ?"
            + " and consent.status = 'CONSENT_SIGNED'"
            + " and now() between consent.start_date and consent.end_date";

    private static final String PERCENTILE = "%";

    private static final String DELIMITER_AMPERSAND = "&";

    private static final String DELIMITER_COLON = ":";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ContextHandlerProperties contextHandlerProperties;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PolicyDtoRowMapper policyDtoRowMapper;
    @Autowired
    private XacmlPolicySetService xacmlPolicySetService;

    JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(dataSource);

    }


    @Override
    public List<Evaluatable> getPolicies(XacmlRequestDto xacmlRequest) throws NoPolicyFoundException, PolicyProviderException {

        final String mrn = xacmlRequest.getPatientId().getExtension();
        final String mrnDomain = xacmlRequest.getPatientId().getRoot();

        final String policyId = toPolicyId(mrn, mrnDomain,
                xacmlRequest.getRecipientNpi(),
                xacmlRequest.getIntermediaryNpi());

        // Get all policies from db
        final PolicyContainerDto policies = getPolicies(policyId);

        //PolicyDto policyDto = new PolicyDto();
        final Evaluatable policySet = xacmlPolicySetService.getPoliciesCombinedAsPolicySet(policies, UUID
                        .randomUUID().toString(),
                PolicyCombiningAlgIds.DENY_OVERRIDES.getUrn());

        // FIXME: REMOVE THIS LINE!!!
        logger.error("JdbcPolicyProviderImpl executed!");

        return Arrays.asList(policySet);

    }

    private String toPolicyId(String pid, String pidDomain,
                              String recipientSubjectNPI, String intermediarySubjectNPI) {
        final StringBuilder policyIdBuilder = new StringBuilder();
        policyIdBuilder.append(pid);
        policyIdBuilder.append(DELIMITER_COLON);
        policyIdBuilder.append(DELIMITER_AMPERSAND);
        policyIdBuilder.append(pidDomain);
        policyIdBuilder.append(DELIMITER_AMPERSAND);
        policyIdBuilder.append(contextHandlerProperties.getPid().getType());
        policyIdBuilder.append(DELIMITER_COLON);
        policyIdBuilder.append(recipientSubjectNPI);
        policyIdBuilder.append(DELIMITER_COLON);
        policyIdBuilder.append(intermediarySubjectNPI);
        policyIdBuilder.append(DELIMITER_COLON);
        policyIdBuilder.append(PERCENTILE);
        return policyIdBuilder.toString();
    }

    private PolicyContainerDto getPolicies(String policyId) {

        // Assert policy id
        assertPolicyId(policyId);

        // get policies from pcm database
        List<PolicyDto> policies = getPoliciesFromDb(policyId);

        // Assert that at least one policy is found
        assertPoliciesNotEmpty(policies, policyId);

        return PolicyContainerDto.builder().policies(policies).build();

    }

    public List<PolicyDto> getPoliciesFromDb(String policyId) {
        jdbcTemplate = getJdbcTemplate();

        List<PolicyDto> policies = this.jdbcTemplate.query(
                SQL_GET_XACML_CONSENT_WC, new Object[]{policyId},
                policyDtoRowMapper);
        logger.info("Consent is queried.");
        return policies;
    }

    //temp method
    @Override
    public ConsentBundleAndPatientDto tempGetFhirConsent(String mrn, String system){

        return null;
    }

}
