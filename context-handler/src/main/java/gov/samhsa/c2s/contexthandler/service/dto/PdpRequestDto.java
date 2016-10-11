package gov.samhsa.c2s.contexthandler.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PdpRequest")
public class PdpRequestDto {

    @XmlElement(name = "SubjectAttributes")
    private List<PdpAttributesDto> subjectAttributes;

    @XmlElement(name = "ResourceAttributes")
    private List<PdpAttributesDto> resourceAttributes;

    @XmlElement(name = "ActionAttributes")
    private List<PdpAttributesDto> actionAttributes;

    @XmlElement(name = "EnvironmentAttributes")
    private List<PdpAttributesDto> environmentAttributes;
}
