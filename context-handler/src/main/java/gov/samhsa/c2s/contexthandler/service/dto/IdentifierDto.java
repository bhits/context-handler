package gov.samhsa.c2s.contexthandler.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.util.Assert;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "identifier")
public class IdentifierDto {
    @NotBlank
    private String oid;
    @NotBlank
    private String value;

    public static IdentifierDto of(String oid, String value) {
        return builder().oid(oid).value(value).build();
    }

    public String getFullIdentifier() {
        Assert.hasText(oid, "oid must exist");
        Assert.hasText(value, "value must exist");
        return value + "^^^&" + oid + "&ISO";
    }
}
