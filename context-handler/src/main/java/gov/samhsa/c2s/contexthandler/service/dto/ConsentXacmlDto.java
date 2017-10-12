package gov.samhsa.c2s.contexthandler.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class ConsentXacmlDto {

    @NotBlank
    private String consentRefId;
    @NotEmpty
    @NotNull
    private byte[] consentXacml;
    @NotBlank
    private String consentXacmlEncoding;
}
