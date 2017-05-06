package gov.samhsa.c2s.contexthandler.service.util;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

public class JAXBUtils {

    public static String marshal(Object obj) throws JAXBException {
        return marshal(obj, obj.getClass());
    }

    private static <T> String marshal(Object obj, Class<T> contextClass)
            throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance(contextClass);
        final Marshaller marshaller = context.createMarshaller();
        final StringWriter stringWriter = new StringWriter();
        marshaller.marshal(obj, stringWriter);
        final String output = stringWriter.toString();
        return output;
    }

    @SuppressWarnings("unchecked")
    public static <T> String marshalWithoutRootElement(T obj)
            throws JAXBException {
        final JAXBElement<T> wrappedJaxbElement = new JAXBElement<>(new QName(
                obj.getClass().getSimpleName()), (Class<T>) obj.getClass(), obj);
        return marshal(wrappedJaxbElement, obj.getClass());
    }

    public static <T> T unmarshalFromXml(Class<T> clazz, String xml)
            throws JAXBException, UnsupportedEncodingException {
        final JAXBContext context = JAXBContext.newInstance(clazz);
        return unmarshalFromXml(context, clazz, xml);
    }

    @SuppressWarnings("unchecked")
    private static <T> T unmarshalFromXml(JAXBContext context, Class<T> clazz,
                                          String xml) throws JAXBException, UnsupportedEncodingException {
        final Unmarshaller um = context.createUnmarshaller();
        final ByteArrayInputStream input = new ByteArrayInputStream(
                xml.getBytes("UTF-8"));
        return (T) um.unmarshal(input);
    }
}
