package gov.samhsa.c2s.contexthandler.service.util;

import gov.samhsa.c2s.common.document.transformer.XmlTransformer;
import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.common.marshaller.SimpleMarshaller;
import gov.samhsa.c2s.contexthandler.config.ContextHandlerProperties;
import gov.samhsa.c2s.contexthandler.config.FhirProperties;
import gov.samhsa.c2s.contexthandler.service.dto.PdpAttributesDto;
import gov.samhsa.c2s.contexthandler.service.dto.PdpRequestDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Component
public class RequestGenerator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String PDPREQUESTXSLNAME = "pdpRequest.xsl";
    /**
     * The xml transformer.
     */
    @Autowired
    private XmlTransformer xmlTransformer;
    @Autowired
    private SimpleMarshaller simpleMarshaller;

    @Autowired
    private ContextHandlerProperties contextHandlerProperties;

    private final FhirProperties fhirProperties;

    @Autowired
    public RequestGenerator(FhirProperties fhirProperties) {
        this.fhirProperties = fhirProperties;
    }

    Function<XacmlRequestDto, PdpRequestDto> XacmlRequestDtoToPdpRequestDto = new Function<XacmlRequestDto, PdpRequestDto>() {
        @Override
        public PdpRequestDto apply(XacmlRequestDto xacmlRequestDto) {
            PdpRequestDto pdpRequestDto = new PdpRequestDto();

            //setting subject attributes
            List<PdpAttributesDto> subjectAttributes = new ArrayList<>();
            subjectAttributes.add(new PdpAttributesDto().builder().attributeId(PdpAttributeIds.SUBJECT_RECIPIENT.getAttributeId())
                    .attributeValue(xacmlRequestDto.getRecipientNpi())
                    .attributeType(PdpAttributeIds.SUBJECT_RECIPIENT.getAttributeType()).build());
            subjectAttributes.add(new PdpAttributesDto().builder().attributeId(PdpAttributeIds.SUBJECT_INTERMEDIARY.getAttributeId())
                    .attributeValue(xacmlRequestDto.getIntermediaryNpi())
                    .attributeType(PdpAttributeIds.SUBJECT_INTERMEDIARY.getAttributeType()).build());

            if(fhirProperties.isEnabled()){
                subjectAttributes.add(new PdpAttributesDto().builder().attributeId(PdpAttributeIds.SUBJECT_POU.getAttributeId())
                        .attributeValue(xacmlRequestDto.getPurposeOfUse().getPurposeFhir())
                        .attributeType(PdpAttributeIds.SUBJECT_POU.getAttributeType()).build());
            }else{
                subjectAttributes.add(new PdpAttributesDto().builder().attributeId(PdpAttributeIds.SUBJECT_POU.getAttributeId())
                        .attributeValue(xacmlRequestDto.getPurposeOfUse().getPurpose())
                        .attributeType(PdpAttributeIds.SUBJECT_POU.getAttributeType()).build());
            }

            pdpRequestDto.setSubjectAttributes(subjectAttributes);

            //setting Resource attributes
            List<PdpAttributesDto> resourceAttributes = new ArrayList<>();
            resourceAttributes.add(new PdpAttributesDto().builder().attributeId(PdpAttributeIds.RESOURCE_TYPECODE.getAttributeId())
                    .attributeValue(contextHandlerProperties.getPdpRequest().getResource().getTypeCode())
                    .attributeType(PdpAttributeIds.RESOURCE_TYPECODE.getAttributeType()).build());
            resourceAttributes.add(new PdpAttributesDto().builder().attributeId(PdpAttributeIds.RESOURCE_STATUS.getAttributeId())
                    .attributeValue(contextHandlerProperties.getPdpRequest().getResource().getStatus())
                    .attributeType(PdpAttributeIds.RESOURCE_STATUS.getAttributeType()).build());

            pdpRequestDto.setResourceAttributes(resourceAttributes);


            //setting Action attributes
            List<PdpAttributesDto> actionAttributes = new ArrayList<>();
            actionAttributes.add(new PdpAttributesDto().builder().attributeId(PdpAttributeIds.ACTION_ACTIONID.getAttributeId())
                    .attributeValue(contextHandlerProperties.getPdpRequest().getAction().getActionId())
                    .attributeType(PdpAttributeIds.ACTION_ACTIONID.getAttributeType()).build());
            pdpRequestDto.setActionAttributes(actionAttributes);

            //setting Environment attributes
            List<PdpAttributesDto> envAttributes = new ArrayList<>();
            envAttributes.add(new PdpAttributesDto().builder().attributeId(PdpAttributeIds.Environment_CURRENTDATETIME.getAttributeId())
                    .attributeValue(getDate())
                    .attributeType(PdpAttributeIds.Environment_CURRENTDATETIME.getAttributeType()).build());
            pdpRequestDto.setEnvironmentAttributes(envAttributes);


            return pdpRequestDto;
        }
    };

    public RequestType generateRequest(XacmlRequestDto xacmlRequest) {
        RequestType requestType = null;
        final String request = generateRequestString(xacmlRequest);
        final InputStream is = new ByteArrayInputStream(request.getBytes());
        try {
            requestType = unmarshalRequest(is);
        } catch (final SyntaxException e) {
            logger.debug(e.getMessage(), e);
        }
        return requestType;
    }

    public String generateRequestString(XacmlRequestDto xacmlRequest) {
        String pdpRequest = "";
        final ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        try {
            PdpRequestDto pdpRequestDto = convertToPdpRequestDto(xacmlRequest);

            logger.debug(() -> createPDPRequestDtoLogMessage(pdpRequestDto));

            pdpRequest = xmlTransformer.transform(pdpRequestDto,
                    classLoader.getResource(PDPREQUESTXSLNAME).toString()
                    , Optional.empty(), Optional.empty());

        } catch (final Exception e) {
            logger.error(e.getMessage());
        }
        return pdpRequest;
    }

    private PdpRequestDto convertToPdpRequestDto(XacmlRequestDto xacmlRequestDto) {
        PdpRequestDto pdpRequestDto = XacmlRequestDtoToPdpRequestDto.apply(xacmlRequestDto);

        return pdpRequestDto;
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
     * @param inputStream the input stream
     * @return the request type
     * @throws SyntaxException the syntax exception
     */
    RequestType unmarshalRequest(InputStream inputStream)
            throws SyntaxException {
        return RequestMarshaller.unmarshal(inputStream);
    }

    private String createPDPRequestDtoLogMessage(PdpRequestDto request) {

        final String logMsgPrefix = "PDP Request DTO: ";
        final String errMsg = "Failed during marshalling PDP Request DTO";
        try {
            return new StringBuilder().append(logMsgPrefix).append(simpleMarshaller.marshal(request)).toString();
        } catch (Exception e) {
            logger.error(() -> new StringBuilder().append(errMsg).append(" : ").append(e.getMessage()).toString());
        }
        return logMsgPrefix + errMsg;
    }


    // Convert this as public static void main(String[] args) to evaluate independently
    private static void testPDPEvaluation() throws UnsupportedEncodingException,
            SyntaxException {
        final PDP pdp = SimplePDPFactory.getSimplePDP();
        //final String workingreq = "<xacml-context:Request xmlns:xacml-context=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\"><xacml-context:Subject SubjectCategory=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\"><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>Mie Physician</xacml-context:AttributeValue></xacml-context:Attribute><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xacml:2.0:subject:role\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>Administrator</xacml-context:AttributeValue></xacml-context:Attribute><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xspa:1.0:subject:organization\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>MIE</xacml-context:AttributeValue></xacml-context:Attribute><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xspa:1.0:subject:organization-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>2.16.840.1.113883.3.704.1.100.102</xacml-context:AttributeValue></xacml-context:Attribute><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xspa:1.0:subject:purposeofuse\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>TREATMENT</xacml-context:AttributeValue></xacml-context:Attribute><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>2222222222</xacml-context:AttributeValue></xacml-context:Attribute><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>1427467752</xacml-context:AttributeValue></xacml-context:Attribute></xacml-context:Subject><Resource xmlns=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\"><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:typeCode\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" XdsId=\"urn:uuid:f0306f51-975f-434e-a61c-c59651d33983\"><AttributeValue>34133-9</AttributeValue></Attribute><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:practiceSettingCode\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" XdsId=\"urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead\"><AttributeValue>Home</AttributeValue></Attribute><Attribute AttributeId=\"xacml:status\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" XdsId=\"status\"><AttributeValue>urn:oasis:names:tc:ebxml-regrep:StatusType:Approved</AttributeValue></Attribute></Resource><xacml-context:Action><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><xacml-context:AttributeValue>xdsquery</xacml-context:AttributeValue></xacml-context:Attribute></xacml-context:Action><xacml-context:Environment><xacml-context:Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\" DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\"><xacml-context:AttributeValue>2014-08-18T18:45:12.633Z</xacml-context:AttributeValue></xacml-context:Attribute></xacml-context:Environment></xacml-context:Request>";
        final String req="<Request xmlns=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\" " +
                "xmlns:ns2=\"urn:oasis:names:tc:xacml:2.0:policy:schema:os\"><Subject><Attribute " +
                "AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject\" " +
                "DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>1003235045</AttributeValue" +
                "></Attribute><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:intermediary" +
                "-subject\" DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\"><AttributeValue>1003092362</AttributeValue></Attribute><Attribute " +
                "AttributeId=\"urn:oasis:names:tc:xspa:1.0:subject:purposeofuse\" DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\"><AttributeValue>TREAT</AttributeValue></Attribute></Subject" +
                "><Resource><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:typeCode\" " +
                "DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>34133-9</AttributeValue" +
                "></Attribute><Attribute AttributeId=\"xacml:status\" DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\"><AttributeValue>urn:oasis:names:tc:ebxml-regrep:StatusType:Approved" +
                "</AttributeValue></Attribute></Resource><Action><Attribute " +
                "AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\"><AttributeValue>pepaccess</AttributeValue></Attribute></Action" +
                "><Environment><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\" " +
                "DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\"><AttributeValue>2017-06-19T04:23:14.367-04:00" +
                "</AttributeValue></Attribute></Environment></Request>";
        /*final String req="<?xml version=\"1.0\" encoding=\"UTF-8\"?><Request xmlns=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><Subject><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>1740515725</AttributeValue></Attribute><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>1346575297</AttributeValue></Attribute><Attribute AttributeId=\"gov.samhsa.consent2share.purpose-of-use-code\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>TREATMENT</AttributeValue></Attribute></Subject><Resource><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:typeCode\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>34133-9</AttributeValue></Attribute><Attribute AttributeId=\"xacml:status\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>urn:oasis:names:tc:ebxml-regrep:StatusType:Approved</AttributeValue></Attribute></Resource><Action><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"><AttributeValue>xdsquery</AttributeValue></Attribute></Action><Environment><Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\" DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\">2015-09-02T04:49:13-0400</AttributeValue></Attribute></Environment></Request>";
       */
        System.out.println(req);
        final RequestType request = RequestMarshaller
                .unmarshal(new ByteArrayInputStream(req.getBytes("UTF8")));
        //final String workingxacml = " <Policy PolicyId=\"REG.1DVRUZMRCA:&amp;2.16.840.1.113883.3.704.100.990.1&amp;ISO:2222222222:1427467752:092d4e2a-3508-4be5-bea6-6c3cdcf085bc\" RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides\" xmlns=\"urn:oasis:names:tc:xacml:2.0:policy:schema:os\"><Description>This is a reference policy for	consent2share@outlook.com</Description><Target/><Rule Effect=\"Permit\" RuleId=\"primary-group-rule\"><Target><Resources><Resource><ResourceMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">34133-9</AttributeValue><ResourceAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:typeCode\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ResourceMatch><ResourceMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">urn:oasis:names:tc:ebxml-regrep:StatusType:Approved</AttributeValue><ResourceAttributeDesignator AttributeId=\"xacml:status\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ResourceMatch></Resource></Resources><Actions><Action><ActionMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">xdsquery</AttributeValue><ActionAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ActionMatch></Action><Action><ActionMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">xdsretrieve</AttributeValue><ActionAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ActionMatch></Action></Actions></Target><Condition><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:and\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">1427467752</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">2222222222</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"urn:oasis:names:tc:xspa:1.0:subject:purposeofuse\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">PAYMENT</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"urn:oasis:names:tc:xspa:1.0:subject:purposeofuse\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">RESEARCH</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"urn:oasis:names:tc:xspa:1.0:subject:purposeofuse\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">TREATMENT</AttributeValue></Apply></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only\"><EnvironmentAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\" DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\" MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\">2014-08-13T00:00:00-0400</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only\"><EnvironmentAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\" DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\" MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\">2015-08-12T23:59:59-0400</AttributeValue></Apply></Apply></Condition></Rule><Rule Effect=\"Deny\" RuleId=\"deny-others\"/><Obligations><Obligation FulfillOn=\"Permit\" ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">PSY</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\" ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">ALC</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\" ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">GDIS</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\" ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">COM</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\" ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">SEX</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\" ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">ETH</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\" ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">ADD</AttributeAssignment></Obligation></Obligations></Policy>";
        final String xacml= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Policy " +
                "xmlns=\"urn:oasis:names:tc:xacml:2.0:policy:schema:os\" PolicyId=\"RgXKtG6hP1\" " +
                "RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides" +
                "\"><Description>\n" +
                "\t\t\t\tThis is a reference policy for\n" +
                "\t\t\t\t</Description><Target/><Rule Effect=\"Permit\" " +
                "RuleId=\"primary-group-rule\"><Target><Resources><Resource><ResourceMatch " +
                "MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\">34133-9</AttributeValue><ResourceAttributeDesignator " +
                "AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:typeCode\" DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\"/></ResourceMatch><ResourceMatch " +
                "MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\">urn:oasis:names:tc:ebxml-regrep:StatusType:Approved</AttributeValue" +
                "><ResourceAttributeDesignator AttributeId=\"xacml:status\" DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\"/></ResourceMatch></Resource></Resources><Actions><Action" +
                "><ActionMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue " +
                "DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\">xdsquery</AttributeValue><ActionAttributeDesignator " +
                "AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\"/></ActionMatch></Action><Action><ActionMatch " +
                "MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\">xdsretrieve</AttributeValue><ActionAttributeDesignator " +
                "AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\"/></ActionMatch></Action><Action><ActionMatch " +
                "MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\">pepaccess</AttributeValue><ActionAttributeDesignator " +
                "AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\"/></ActionMatch></Action></Actions></Target><Condition><Apply " +
                "FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:and\"><Apply " +
                "FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\"><Apply " +
                "FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply " +
                "FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator" +
                " MustBePresent=\"false\" AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:intermediary" +
                "-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></Apply><AttributeValue " +
                "DataType=\"http://www.w3.org/2001/XMLSchema#string\">1003092362</AttributeValue></Apply></Apply" +
                "><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\"/><Apply " +
                "FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\"><Apply " +
                "FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply " +
                "FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator" +
                " MustBePresent=\"false\" AttributeId=\"urn:oasis:names:tc:xspa:1.0:subject:purposeofuse\" " +
                "DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></Apply><AttributeValue DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\">TREAT</AttributeValue></Apply></Apply><Apply " +
                "FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal\"><Apply " +
                "FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only" +
                "\"><EnvironmentAttributeDesignator MustBePresent=\"false\" " +
                "AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\" DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#dateTime\"/></Apply><AttributeValue DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#dateTime\">2017-06-16T00:00:00-0400</AttributeValue></Apply><Apply " +
                "FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal\"><Apply " +
                "FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only" +
                "\"><EnvironmentAttributeDesignator MustBePresent=\"false\" " +
                "AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\" DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#dateTime\"/></Apply><AttributeValue DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#dateTime\">2018-06-16T00:00:00-0400</AttributeValue></Apply></Apply" +
                "></Condition></Rule><Rule Effect=\"Deny\" RuleId=\"deny-others\"/><Obligations><Obligation " +
                "ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:share-sensitivity-policy-code\" " +
                "FulfillOn=\"Permit\"><AttributeAssignment " +
                "AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\">ALC</AttributeAssignment></Obligation><Obligation " +
                "ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:share-sensitivity-policy-code\" " +
                "FulfillOn=\"Permit\"><AttributeAssignment " +
                "AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\" DataType=\"http://www" +
                ".w3.org/2001/XMLSchema#string\">PSY</AttributeAssignment></Obligation></Obligations></Policy>";
      /*  final String xacml= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><PolicySet xmlns=\"urn:oasis:names:tc:xacml:2.0:policy:schema:os\"           xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"           PolicyCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides\"           PolicySetId=\"b0c9bb33-f55a-4d88-92d5-a41292967741\"           xsi:schemaLocation=\"urn:oasis:names:tc:xacml:2.0:policy:schema:os http://docs.oasis-open.org/xacml/access_control-xacml-2.0-policy-schema-os.xsd\"><Description/><Target/><Policy PolicyId=\"C2S.QA.H7ICTU:&amp;2.16.840.1.113883.3.72.5.9.4&amp;ISO:1740515725:1346575297:MPL8V8\"           RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides\"><Description>				This is a reference policy for				ASAMPLE  PATIENTTWO</Description><Target/><Rule Effect=\"Permit\" RuleId=\"primary-group-rule\"><Target><Resources><Resource><ResourceMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">34133-9</AttributeValue><ResourceAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:typeCode\"                                                  DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ResourceMatch><ResourceMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">urn:oasis:names:tc:ebxml-regrep:StatusType:Approved								</AttributeValue><ResourceAttributeDesignator AttributeId=\"xacml:status\"                                                  DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ResourceMatch></Resource></Resources><Actions><Action><ActionMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">xdsquery								</AttributeValue><ActionAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\"                                                DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ActionMatch></Action><Action><ActionMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">xdsretrieve								</AttributeValue><ActionAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\"                                                DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></ActionMatch></Action></Actions></Target><Condition><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:and\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject\"                                                    DataType=\"http://www.w3.org/2001/XMLSchema#string\"                                                    MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">1346575297</AttributeValue></Apply></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject\"                                                    DataType=\"http://www.w3.org/2001/XMLSchema#string\"                                                    MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">1740515725</AttributeValue></Apply></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"gov.samhsa.consent2share.purpose-of-use-code\"                                                    DataType=\"http://www.w3.org/2001/XMLSchema#string\"                                                    MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">TREATMENT</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><SubjectAttributeDesignator AttributeId=\"gov.samhsa.consent2share.purpose-of-use-code\"                                                    DataType=\"http://www.w3.org/2001/XMLSchema#string\"                                                    MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">PAYMENT</AttributeValue></Apply></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only\"><EnvironmentAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\"                                                     DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\"                                                     MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\">2015-09-01T00:00:00-0400</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only\"><EnvironmentAttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime\"                                                     DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\"                                                     MustBePresent=\"false\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#dateTime\">2016-08-26T23:59:59-0400</AttributeValue></Apply></Apply></Condition></Rule><Rule Effect=\"Deny\" RuleId=\"deny-others\"/><Obligations><Obligation FulfillOn=\"Permit\"                     ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\"                                 DataType=\"http://www.w3.org/2001/XMLSchema#string\">SEX</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\"                     ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\"                                 DataType=\"http://www.w3.org/2001/XMLSchema#string\">PSY</AttributeAssignment></Obligation><Obligation FulfillOn=\"Permit\"                     ObligationId=\"urn:samhsa:names:tc:consent2share:1.0:obligation:redact-document-section-code\"><AttributeAssignment AttributeId=\"urn:oasis:names:tc:xacml:3.0:example:attribute:text\"                                 DataType=\"http://www.w3.org/2001/XMLSchema#string\">HIV</AttributeAssignment></Obligation></Obligations></Policy></PolicySet>";
      */  final Evaluatable e = PolicyMarshaller
                .unmarshal(new ByteArrayInputStream(xacml.getBytes("UTF8")));

        final PolicyRetrievalPoint repo = pdp.getPolicyRepository();
        final UnorderedPolicyRepository repository = (UnorderedPolicyRepository) repo;
        repository.deploy(Arrays.asList(e));

        final ResponseType resp = pdp.evaluate(request);
        System.out.println(resp.getResults().get(0).getDecision().toString());
    }

}
