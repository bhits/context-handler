package gov.samhsa.c2s.contexthandler.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by sadhana.chandra on 6/23/2016.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdpAttributesDto {

    private String attributeId;
    private String attributeValue;
    private String attributeType;
}
