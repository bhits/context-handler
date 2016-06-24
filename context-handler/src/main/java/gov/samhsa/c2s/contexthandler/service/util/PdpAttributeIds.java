package gov.samhsa.c2s.contexthandler.service.util;

/**
 * Created by sadhana.chandra on 6/23/2016.
 */
public enum PdpAttributeIds {

    SUBJECT_RECIPIENT("urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject"),
    SUBJECT_INTERMEDIARY("urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject"),
    SUBJECT_POU("urn:oasis:names:tc:xspa:1.0:subject:purposeofuse"),
    RESOURCE_TYPECODE("urn:oasis:names:tc:xacml:1.0:resource:typeCode"),
    RESOURCE_STATUS("xacml:status"),
    ACTION_ACTIONID("urn:oasis:names:tc:xacml:1.0:action:action-id"),
    Environment_CURRENTDATETIME("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime","http://www.w3.org/2001/XMLSchema#dateTime");

    private final String attributeId;

    private final String attributeType;

    PdpAttributeIds(String id, String type){
        attributeId = id;
        attributeType = type;
    }
    PdpAttributeIds(String id){
        attributeId = id;
        attributeType = "http://www.w3.org/2001/XMLSchema#string";
    }
    public String getAttributeId() {
        return attributeId;
    }

    public String getAttributeType() {
        return attributeType;
    }
}
