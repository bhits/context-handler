package gov.samhsa.c2s.contexthandler.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PatientId")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientIdDto {
    /**
     * The patient id.
     */
    @XmlElement(name = "Root")
    @NotBlank
    private String root;

    /**
     * The patient id.
     */
    @XmlElement(name = "Extension")
    @NotBlank
    private String extension;
}
