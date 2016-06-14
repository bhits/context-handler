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
package gov.samhsa.c2s.contexthandler.service.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import gov.samhsa.c2s.contexthandler.service.XacmlXslUrlProviderImpl;
import gov.samhsa.c2s.contexthandler.service.XslResource;
import gov.samhsa.c2s.contexthandler.service.dto.PatientIdDto;
import gov.samhsa.c2s.contexthandler.service.dto.SubjectPurposeOfUse;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.mhc.common.document.transformer.XmlTransformer;
import gov.samhsa.mhc.common.document.transformer.XmlTransformerImpl;
import gov.samhsa.mhc.common.marshaller.SimpleMarshallerImpl;
import org.herasaf.xacml.core.SyntaxException;
import org.herasaf.xacml.core.api.PDP;
import org.herasaf.xacml.core.api.PolicyRetrievalPoint;
import org.herasaf.xacml.core.api.UnorderedPolicyRepository;
import org.herasaf.xacml.core.context.RequestMarshaller;
import org.herasaf.xacml.core.context.impl.RequestType;
import org.herasaf.xacml.core.context.impl.ResponseType;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.herasaf.xacml.core.policy.PolicyMarshaller;
import org.herasaf.xacml.core.simplePDP.SimplePDPFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The Class RequestGenerator.
 */
@Component
public class RequestGenerator {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/** The xml transformer. */
	private XmlTransformer xmlTransformer = new XmlTransformerImpl(
			new SimpleMarshallerImpl());
	

	public RequestType generateRequest(XacmlRequestDto xacmlRequest) {
		RequestType requestType = null;
		//XacmlRequest xacmlRequest;

		final String request = generateRequestString(xacmlRequest);
		final InputStream is = new ByteArrayInputStream(request.getBytes());
		try {
			// Need call SimplePDPFactory.getSimplePDP() to use
			// RequestMarshaller from herasaf
			requestType = unmarshalRequest(is);
		} catch (final SyntaxException e) {
			logger.debug(e.getMessage(), e);
		}
		return requestType;
	}


	public String generateRequestString(XacmlRequestDto xacmlRequest) {
		String pdpRequest="";
		try {
			XacmlXslUrlProviderImpl xacmlXslUrlProvider = new XacmlXslUrlProviderImpl();
			pdpRequest = xmlTransformer.transform( xacmlRequest,
					xacmlXslUrlProvider.getUrl(XslResource.PDPREQUESTXSLNAME), Optional.empty(),Optional.empty());
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return pdpRequest;
	}
	/**
	 * Generate request string.
	 *
	 * @param recepientSubjectNPI
	 *            the recepient subject npi
	 * @param intermediarySubjectNPI
	 *            the intermediary subject npi
	 * @param purposeOfUse
	 *            the purpose of use
	 * @param patientId
	 *            the patient id
	 * @return the string
	 */
	public String generateRequestString(String recepientSubjectNPI,
			String intermediarySubjectNPI, String purposeOfUse, String patientId) {
		final String date = getDate();

		final StringBuilder requestStringBuilder = new StringBuilder();
		requestStringBuilder
				.append("<Request xmlns=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\"     ");
		requestStringBuilder
				.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">     <Subject>      ");
		requestStringBuilder
				.append("<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject\"       ");
		requestStringBuilder
				.append("DataType=\"http://www.w3.org/2001/XMLSchema#string\">       ");
		requestStringBuilder.append("<AttributeValue>");
		requestStringBuilder.append(recepientSubjectNPI);
		requestStringBuilder
				.append("</AttributeValue>      </Attribute>      ");
		requestStringBuilder
				.append("<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject\"       ");
		requestStringBuilder
				.append("DataType=\"http://www.w3.org/2001/XMLSchema#string\">       <AttributeValue>");
		requestStringBuilder.append(intermediarySubjectNPI);
		requestStringBuilder.append("</AttributeValue>      ");
		requestStringBuilder
				.append("</Attribute>	  <Attribute AttributeId=\"gov.samhsa.consent2share.purpose-of-use-code\"       ");
		requestStringBuilder
				.append("DataType=\"http://www.w3.org/2001/XMLSchema#string\">       <AttributeValue>");
		requestStringBuilder.append(purposeOfUse);
		requestStringBuilder.append("</AttributeValue>      ");
		requestStringBuilder
				.append("</Attribute>     </Subject>     <Resource>      ");
		requestStringBuilder
				.append("<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\"       ");
		requestStringBuilder
				.append("DataType=\"http://www.w3.org/2001/XMLSchema#string\">       ");
		requestStringBuilder.append("<AttributeValue>");
		requestStringBuilder.append(patientId);
		requestStringBuilder
				.append("</AttributeValue>      </Attribute>     </Resource>     ");
		requestStringBuilder
				.append("<Action>      <Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\"       ");
		requestStringBuilder
				.append("DataType=\"http://www.w3.org/2001/XMLSchema#string\">       <AttributeValue>write</AttributeValue>      ");
		requestStringBuilder
				.append("</Attribute>     </Action>     <Environment>		");
		requestStringBuilder
				.append("<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\"       ");
		requestStringBuilder
				.append("DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\">       ");
		requestStringBuilder.append("<AttributeValue>");
		requestStringBuilder.append(date);
		requestStringBuilder
				.append("</AttributeValue>      </Attribute>	 </Environment>    </Request>");

		return requestStringBuilder.toString();
	}

	/**
	 * Gets the current date.
	 *
	 * @return the date
	 */
	public String getDate() {
		final SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZ");
		return sdf.format(new Date());
	}

	/**
	 * Unmarshal request.
	 *
	 * @param inputStream
	 *            the input stream
	 * @return the request type
	 * @throws SyntaxException
	 *             the syntax exception
	 */
	RequestType unmarshalRequest(InputStream inputStream)
			throws SyntaxException {
		return RequestMarshaller.unmarshal(inputStream);
	}
	
	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws SyntaxException
	 *             the syntax exception
	 */
	public static void main(String[] args) throws UnsupportedEncodingException,
	SyntaxException {
		final PDP pdp = SimplePDPFactory.getSimplePDP();
		//final String workingreq = "<xacml-context:Request xmlns:xacml-context=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\"><xacml-context:Subject SubjectCategory=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\"><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>Mie Physician</xacml-context:AttributeValue></xacml-context:Attribute><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xacml:2.0:subject:role\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>Administrator</xacml-context:AttributeValue></xacml-context:Attribute><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xspa:1.0:subject:organization\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>MIE</xacml-context:AttributeValue></xacml-context:Attribute><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xspa:1.0:subject:organization-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>2.16.840.1.113883.3.704.1.100.102</xacml-context:AttributeValue></xacml-context:Attribute><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xspa:1.0:subject:purposeofuse\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>TREATMENT</xacml-context:AttributeValue></xacml-context:Attribute><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>2222222222</xacml-context:AttributeValue></xacml-context:Attribute><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>1427467752</xacml-context:AttributeValue></xacml-context:Attribute></xacml-context:Subject><Resource xmlns=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\"><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:typeCode\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" XdsId=\"urn:uuid:f0306f51-975f-434e-a61c-c59651d33983\"><AttributeValue>34133-9</AttributeValue></Attribute><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:practiceSettingCode\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" XdsId=\"urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead\"><AttributeValue>Home</AttributeValue></Attribute><Attribute AttributeId=\"xacml:status\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" XdsId=\"status\"><AttributeValue>urn:oasis:names:tc:ebxml-regrep:StatusType:Approved</AttributeValue></Attribute></Resource><xacml-context:Action><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>xdsquery</xacml-context:AttributeValue></xacml-context:Attribute></xacml-context:Action><xacml-context:Environment><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\" DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\"><xacml-context:AttributeValue>2014-08-18T18:45:12.633Z</xacml-context:AttributeValue></xacml-context:Attribute></xacml-context:Environment></xacml-context:Request>";
		//final String req="<Request xmlns=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\"     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Subject><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject\"       DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>1972838936</AttributeValue></Attribute><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject\"       DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>1740515725</AttributeValue></Attribute><Attribute AttributeId=\"gov.samhsa.consent2share.purpose-of-use-code\"       DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>TREATMENT</AttributeValue></Attribute></Subject><Resource><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:typeCode\"       DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>34133-9</AttributeValue></Attribute><Attribute AttributeId=\"xacml:status\"     DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>Approved</AttributeValue></Attribute>		</Resource><Action><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\"       DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>write</AttributeValue></Attribute></Action><Environment><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\"       DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\"><AttributeValue>2015-08-18T16:52:15-0400</AttributeValue></Attribute></Environment></Request>";
		final String req="<?xml version=\"1.0\" encoding=\"UTF-8\"?><Request xmlns=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Subject><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>1740515725</AttributeValue></Attribute><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>1346575297</AttributeValue></Attribute><Attribute AttributeId=\"gov.samhsa.consent2share.purpose-of-use-code\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>TREATMENT</AttributeValue></Attribute></Subject><Resource><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:typeCode\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>34133-9</AttributeValue></Attribute><Attribute AttributeId=\"xacml:status\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>urn:oasis:names:tc:ebxml-regrep:StatusType:Approved</AttributeValue></Attribute></Resource><Action><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>xdsquery</AttributeValue></Attribute></Action><Environment><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\" DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\">2015-09-02T04:49:13-0400</AttributeValue></Attribute></Environment></Request>";
		System.out.println(req);
		final RequestType request = RequestMarshaller
				.unmarshal(new ByteArrayInputStream(req.getBytes("UTF8")));
		//final String workingxacml = " <Policy PolicyId=\"REG.1DVRUZMRCA:&amp;2.16.840.1.113883.3.704.100.990.1&amp;ISO:2222222222:1427467752:092d4e2a-3508-4be5-bea6-6c3cdcf085bc\" RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides\" xmlns=\"urn:oasis:names:tc:xacml:2.0:policy:schema:os\"><Description>This is a reference policy for	consent2share@outlook.com</Description><Target/><Rule Effect=\"Permit\" RuleId=\"primary-group-rule\"><Target><Resources><Resource><ResourceMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">34133-9</AttributeValue><ResourceAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:typeCode\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ResourceMatch><ResourceMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">urn:oasis:names:tc:ebxml-regrep:StatusType:Approved</AttributeValue><ResourceAttributeDesignator AttributeId=\"xacml:status\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ResourceMatch></Resource></Resources><Actions><Action><ActionMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">xdsquery</AttributeValue><ActionAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ActionMatch></Action><Action><ActionMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">xdsretrieve</AttributeValue><ActionAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ActionMatch></Action></Actions></Target><Condition><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:and\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">1427467752</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">2222222222</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"urn:oasis:names:tc:xspa:1.0:subject:purposeofuse\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">PAYMENT</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"urn:oasis:names:tc:xspa:1.0:subject:purposeofuse\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">RESEARCH</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"urn:oasis:names:tc:xspa:1.0:subject:purposeofuse\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">TREATMENT</AttributeValue></Apply></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only\"><EnvironmentAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\" DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\" MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\">2014-08-13T00:00:00-0400</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only\"><EnvironmentAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\" DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\" MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\">2015-08-12T23:59:59-0400</AttributeValue></Apply></Apply></Condition></Rule><Rule Effect=\"Deny\" RuleId=\"deny-others\"/><Obligations><Obligation FulfillOn=\"Permit\" ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">PSY</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\" ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">ALC</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\" ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">GDIS</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\" ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">COM</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\" ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">SEX</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\" ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">ETH</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\" ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">ADD</AttributeAssignment></Obligation></Obligations></Policy>";
		//final String xacml= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><PolicySet xmlns=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">urn:oasis:names:tc:ebxml-regrep:StatusType:Approved</AttributeValue><ResourceAttributeDesignator AttributeId=\"xacml:status\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ResourceMatch></Resource></Resources><Actions><Action><ActionMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">xdsquery</AttributeValue><ActionAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ActionMatch></Action><Action><ActionMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">xdsretrieve</AttributeValue><ActionAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ActionMatch></Action></Actions></Target><Condition><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:and\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator MustBePresent=\"false\" AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">1972838936</AttributeValue></Apply></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator MustBePresent=\"false\" AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">1740515725</AttributeValue></Apply></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator MustBePresent=\"false\" AttributeId=\"gov.samhsa.consent2share.purpose-of-use-code\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">PAYMENT</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator MustBePresent=\"false\" AttributeId=\"gov.samhsa.consent2share.purpose-of-use-code\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">TREATMENT</AttributeValue></Apply></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only\"><EnvironmentAttributeDesignator MustBePresent=\"false\" AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\" DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\">2015-08-19T00:00:00-0400</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only\"><EnvironmentAttributeDesignator MustBePresent=\"false\" AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\" DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\">2016-08-18T23:59:59-0400</AttributeValue></Apply></Apply></Condition></Rule><Rule Effect=\"Deny\" RuleId=\"deny-others\"/><Obligations><Obligation ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\" FulfillOn=\"Permit\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">ALC</AttributeAssignment></Obligation><Obligation ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\" FulfillOn=\"Permit\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">PSY</AttributeAssignment></Obligation><Obligation ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\" FulfillOn=\"Permit\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">SEX</AttributeAssignment></Obligation></Obligations></Policy></PolicySet>";
		final String xacml= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><PolicySet xmlns=\"urn:oasis:names:tc:xacml:2.0:policy:schema:os\"           xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"           PolicyCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides\"           PolicySetId=\"b0c9bb33-f55a-4d88-92d5-a41292967741\"           xsi:schemaLocation=\"urn:oasis:names:tc:xacml:2.0:policy:schema:os http://docs.oasis-open.org/xacml/access_control-xacml-2.0-policy-schema-os.xsd\"><Description/><Target/><Policy PolicyId=\"C2S.QA.H7ICTU:&amp;2.16.840.1.113883.3.72.5.9.4&amp;ISO:1740515725:1346575297:MPL8V8\"           RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides\"><Description>				This is a reference policy for				ASAMPLE  PATIENTTWO</Description><Target/><Rule Effect=\"Permit\" RuleId=\"primary-group-rule\"><Target><Resources><Resource><ResourceMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">34133-9</AttributeValue><ResourceAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:typeCode\"                                                  DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ResourceMatch><ResourceMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">urn:oasis:names:tc:ebxml-regrep:StatusType:Approved								</AttributeValue><ResourceAttributeDesignator AttributeId=\"xacml:status\"                                                  DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ResourceMatch></Resource></Resources><Actions><Action><ActionMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">xdsquery								</AttributeValue><ActionAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\"                                                DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ActionMatch></Action><Action><ActionMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">xdsretrieve								</AttributeValue><ActionAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\"                                                DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ActionMatch></Action></Actions></Target><Condition><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:and\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject\"                                                    DataType=\"http://www.w3.org/2001/XMLSchema#string\"                                                    MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">1346575297</AttributeValue></Apply></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject\"                                                    DataType=\"http://www.w3.org/2001/XMLSchema#string\"                                                    MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">1740515725</AttributeValue></Apply></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"gov.samhsa.consent2share.purpose-of-use-code\"                                                    DataType=\"http://www.w3.org/2001/XMLSchema#string\"                                                    MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">TREATMENT</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"gov.samhsa.consent2share.purpose-of-use-code\"                                                    DataType=\"http://www.w3.org/2001/XMLSchema#string\"                                                    MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">PAYMENT</AttributeValue></Apply></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only\"><EnvironmentAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\"                                                     DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\"                                                     MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\">2015-09-01T00:00:00-0400</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only\"><EnvironmentAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\"                                                     DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\"                                                     MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\">2016-08-26T23:59:59-0400</AttributeValue></Apply></Apply></Condition></Rule><Rule Effect=\"Deny\" RuleId=\"deny-others\"/><Obligations><Obligation FulfillOn=\"Permit\"                     ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\"                                 DataType=\"http://www.w3.org/2001/XMLSchema#string\">SEX</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\"                     ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\"                                 DataType=\"http://www.w3.org/2001/XMLSchema#string\">PSY</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\"                     ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\"                                 DataType=\"http://www.w3.org/2001/XMLSchema#string\">HIV</AttributeAssignment></Obligation></Obligations></Policy></PolicySet>";
		final Evaluatable e = PolicyMarshaller
				.unmarshal(new ByteArrayInputStream(xacml.getBytes("UTF8")));

		final PolicyRetrievalPoint repo = pdp.getPolicyRepository();
		final UnorderedPolicyRepository repository = (UnorderedPolicyRepository) repo;
		repository.deploy(Arrays.asList(e));

		final ResponseType resp = pdp.evaluate(request);
		System.out.println(resp.getResults().get(0).getDecision().toString());
	}
	
	public static void main1(String[] args) throws UnsupportedEncodingException,
	SyntaxException {
		RequestGenerator rg = new RequestGenerator();
		final XacmlRequestDto xacmlRequest = new XacmlRequestDto();
		xacmlRequest
		.setIntermediaryNpi("1740515725");
		PatientIdDto patientIdDto=new PatientIdDto();
		patientIdDto.setExtension("e3efd9a0-3554-11e5-b70e-00155dc93b18");
		patientIdDto.setRoot("2.16.840.1.113883.4.357");
		//xacmlRequest.setPatientUniqueId("'e3efd9a0-3554-11e5-b70e-00155dc93b18^^^&amp;2.16.840.1.113883.4.357&amp;ISO'");
		xacmlRequest.setPurposeOfUse(SubjectPurposeOfUse.HEALTHCARE_TREATMENT);
		xacmlRequest.setRecipientNpi("1972838936");
		
		RequestType pdpReq = rg.generateRequest(xacmlRequest);
		System.out.println(pdpReq);
		

	}
}
