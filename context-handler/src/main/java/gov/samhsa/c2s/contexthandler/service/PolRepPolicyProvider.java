/*******************************************************************************
 * Open Behavioral Health Information Technology Architecture (OBHITA.org)
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *       * Redistributions of source code must retain the above copyright
 *         notice, this list of conditions and the following disclaimer.
 *       * Redistributions in binary form must reproduce the above copyright
 *         notice, this list of conditions and the following disclaimer in the
 *         documentation and/or other materials provided with the distribution.
 *       * Neither the name of the <organization> nor the
 *         names of its contributors may be used to endorse or promote products
 *         derived from this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *   ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 *   DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *   (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.samhsa.c2s.contexthandler.service;



import gov.samhsa.c2s.contexthandler.service.dto.PolicyDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;
import org.herasaf.xacml.core.SyntaxException;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.herasaf.xacml.core.policy.PolicyMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

/**
 * The Class PolRepPolicyProvider.
 */
@Service
public class PolRepPolicyProvider implements PolicyProvider {
	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/** The Constant WILDCARD. */
	private static final String WILDCARD = "*";

	/** The Constant DELIMITER_AMPERSAND. */
	private static final String DELIMITER_AMPERSAND = "&";

	/** The Constant DELIMITER_COLON. */
	private static final String DELIMITER_COLON = ":";

	/** The pid domain. */
	private String pidDomain;

	/** The pid domain type. */
	private String pidDomainType;

	@Override
	public List<Evaluatable> getPolicies(XacmlRequestDto xacmlRequest)
			throws NoPolicyFoundException, PolicyProviderException {
		try {
			final String mrn = xacmlRequest.getPatientId().getExtension();
			final String mrnDomain = xacmlRequest.getPatientId().getRoot();

			final String policyId = toPolicyId(mrn, pidDomain,
					xacmlRequest.getRecipientNpi(),
					xacmlRequest.getIntermediaryNpi());
			PolicyDto policyDto = new PolicyDto();
/*			final PolicyDto policyDto = polRepRestClient
					.getPoliciesCombinedAsPolicySet(policyId, WILDCARD, UUID
									.randomUUID().toString(),
							PolicyCombiningAlgIds.DENY_OVERRIDES);*/
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
		policyIdBuilder.append(WILDCARD);
		return policyIdBuilder.toString();
	}
}
