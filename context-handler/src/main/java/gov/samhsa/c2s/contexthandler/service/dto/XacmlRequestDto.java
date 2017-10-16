package gov.samhsa.c2s.contexthandler.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "XacmlRequest")
public class XacmlRequestDto {

    @XmlElement(name = "RecipientNpi")
    @NotBlank
    private String recipientNpi;

    @XmlElement(name = "IntermediaryNpi")
    @NotBlank
    private String intermediaryNpi;

    @XmlElement(name = "PurposeOfUse")
    @NotNull
    private SubjectPurposeOfUse purposeOfUse;

    @XmlElement(name = "PatientId")
    @NotNull
    private PatientIdDto patientId;

    @XmlElement(name = "MessageId")
    private String messageId;

}
