package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.contexthandler.service.dto.PolicyDto;
import gov.samhsa.c2s.contexthandler.service.util.PolicyDtoRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;

/**
 * Created by sadhana.chandra on 6/16/2016.
 */
@Service
public class PolicyGetterServiceImpl implements PolicyGetterService {

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    /** The signed consent dto row mapper. */
    @Autowired
    private PolicyDtoRowMapper policyDtoRowMapper;

    /**
     * The Constant SQL_GET_SIGNED_CONSENT.
     * consent_reference_id, xacml_ccd,consent
     */
    //TODO : Need to add more filter items like not to get revoke , expire policy etc..
    public static final String SQL_GET_XACML_CONSENT_WC = "select consent.consent_reference_id, consent.xacml_ccd  "
            + " from consent "
            + " where consent.consent_reference_id like ?";


    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<PolicyDto> getPolicies(String policyId) {
        jdbcTemplate = getJdbcTemplate();

        List<PolicyDto> policies = this.jdbcTemplate.query(
                    SQL_GET_XACML_CONSENT_WC, new Object[]{policyId},
                    policyDtoRowMapper);
        logger.info("Consent is queried.");
        return policies;
    }

    /**
     * Gets the jdbc template.
     *
     * @return the jdbc template
     */
    JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(dataSource);

    }
}
