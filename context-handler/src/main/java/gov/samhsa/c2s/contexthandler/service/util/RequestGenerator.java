/*******************************************************************************
 * Open Behavioral Health Information Technology Architecture (OBHITA.org)
 * <p>
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
 * <p>
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

import gov.samhsa.c2s.common.document.transformer.XmlTransformer;
import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.common.marshaller.SimpleMarshaller;
import gov.samhsa.c2s.contexthandler.config.ContextHandlerProperties;
import gov.samhsa.c2s.contexthandler.service.dto.PdpAttributesDto;
import gov.samhsa.c2s.contexthandler.service.dto.PdpRequestDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import org.herasaf.xacml.core.SyntaxException;
import org.herasaf.xacml.core.context.RequestMarshaller;
import org.herasaf.xacml.core.context.impl.RequestType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
            subjectAttributes.add(new PdpAttributesDto().builder().attributeId(PdpAttributeIds.SUBJECT_POU.getAttributeId())
                    .attributeValue(xacmlRequestDto.getPurposeOfUse().getPurpose())
                    .attributeType(PdpAttributeIds.SUBJECT_POU.getAttributeType()).build());

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
}
