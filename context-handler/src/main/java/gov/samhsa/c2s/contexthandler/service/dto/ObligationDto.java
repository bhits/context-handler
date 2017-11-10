package gov.samhsa.c2s.contexthandler.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class ObligationDto {
    @NotBlank
    private String obligationId;
    @NotBlank
    private String obligationValue;
}
