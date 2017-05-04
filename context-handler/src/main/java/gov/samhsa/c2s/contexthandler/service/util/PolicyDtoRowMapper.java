package gov.samhsa.c2s.contexthandler.service.util;

import gov.samhsa.c2s.contexthandler.service.dto.PolicyDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The Class PolicyDtoRowMapper.
 */
@Component
public class PolicyDtoRowMapper implements RowMapper<PolicyDto> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The lob handler. */
    @Autowired
    private LobHandler lobHandler;

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet,
     * int)
     */
    @Override
    public PolicyDto mapRow(ResultSet rs, int i) throws SQLException {
        String xacmlCcdId = rs.getString("consent.consent_reference_id");
        byte[] xacmlCcd = lobHandler.getBlobAsBytes(rs, "consent.xacml_ccd");
        logger.debug("Consent File:\n" + (new String(xacmlCcd)));

        PolicyDto policyDto = new PolicyDto();
        policyDto.setPolicy(xacmlCcd);
        policyDto.setId(xacmlCcdId);
        return policyDto;
    }
}
