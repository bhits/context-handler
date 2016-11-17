package gov.samhsa.c2s.contexthandler.config;


import ch.qos.logback.audit.AuditException;
import gov.samhsa.c2s.common.audit.AuditClient;
import gov.samhsa.c2s.common.audit.AuditClientImpl;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorImpl;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverterImpl;
import gov.samhsa.c2s.common.document.transformer.XmlTransformer;
import gov.samhsa.c2s.common.document.transformer.XmlTransformerImpl;
import gov.samhsa.c2s.common.marshaller.SimpleMarshaller;
import gov.samhsa.c2s.common.marshaller.SimpleMarshallerImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

@Configuration
public class ApplicationContextConfig {

    @Bean
    @ConditionalOnBean(AuditClientProperties.class)
    public AuditClient auditClient(AuditClientProperties auditClientProperties) throws AuditException {
        return new AuditClientImpl("ContextHandlerAuditClient", auditClientProperties.getHost(), auditClientProperties.getPort());
    }

    @Bean
    public DocumentXmlConverter documentXmlConverter() {
        return new DocumentXmlConverterImpl();
    }

    @Bean
    public DocumentAccessor documentAccessor() {
        return new DocumentAccessorImpl();
    }


    @Bean
    public SimpleMarshaller simpleMarshaller() {
        return new SimpleMarshallerImpl();
    }

    @Bean
    public XmlTransformer xmlTransformer() {
        return new XmlTransformerImpl(simpleMarshaller());
    }

    @Bean
    public LobHandler lobHandler() {
        DefaultLobHandler defaultLobHandler = new DefaultLobHandler();
        defaultLobHandler.setStreamAsLob(true);
        return defaultLobHandler;
    }
}
