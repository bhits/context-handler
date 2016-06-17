package gov.samhsa.c2s.contexthandler.service.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="XacmlRequest")
public class XacmlRequestDto {

    /** The recipient subject npi. */
    @XmlElement(name="RecipientNpi")
    @NotBlank
    private String recipientNpi;

    /** The intermediary subject npi. */
    @XmlElement(name="IntermediaryNpi")
    @NotBlank
    private String intermediaryNpi;

    /** The purpose of use. */
    @XmlElement(name="PurposeOfUse")
    @NotNull
    private SubjectPurposeOfUse purposeOfUse;

    /** The patient id. */
    @XmlElement(name="PatientId")
    @NotNull
    private PatientIdDto patientId;

    /** The message id. */
    @XmlElement(name="MessageId")
    private String messageId;
}
