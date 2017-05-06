package gov.samhsa.c2s.contexthandler.service.dto;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum SubjectPurposeOfUse {
    @XmlEnumValue("TREATMENT")
    HEALTHCARE_TREATMENT("TREATMENT", "TREAT"),
    @XmlEnumValue("PAYMENT")
    PAYMENT("PAYMENT", "HPAYMT"),
    @XmlEnumValue("RESEARCH")
    RESEARCH("RESEARCH", "HRESCH");

    private final String purpose;
    private final String purposeFhir;

    SubjectPurposeOfUse(String p, String purposeFhir) {
        this.purpose = p;
        this.purposeFhir = purposeFhir;
    }

    public static SubjectPurposeOfUse fromValue(String v) {
        return valueOf(v);
    }

    public static SubjectPurposeOfUse fromPurpose(String purposeOfUse) {
        for (SubjectPurposeOfUse p : SubjectPurposeOfUse.values()) {
            if (p.getPurpose().equals(purposeOfUse)) {
                return p;
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append("The abbreviation '");
        builder.append(purposeOfUse);
        builder.append("' is not defined in this enum.");
        throw new IllegalArgumentException(builder.toString());
    }

    public static SubjectPurposeOfUse fromPurposeFhir(String purposeOfUse) {
        for (SubjectPurposeOfUse p : SubjectPurposeOfUse.values()) {
            if (p.getPurposeFhir().equals(purposeOfUse)) {
                return p;
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append("The abbreviation '");
        builder.append(purposeOfUse);
        builder.append("' is not defined in this enum.");
        throw new IllegalArgumentException(builder.toString());
    }


    public String getPurpose() {
        return purpose;
    }

    public String getPurposeFhir() {
        return purposeFhir;
    }
}
