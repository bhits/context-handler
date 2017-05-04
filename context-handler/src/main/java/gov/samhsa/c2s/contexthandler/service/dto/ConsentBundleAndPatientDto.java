package gov.samhsa.c2s.contexthandler.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsentBundleAndPatientDto {
    private Bundle consentSearchResponse;
    private Patient patient;
}
