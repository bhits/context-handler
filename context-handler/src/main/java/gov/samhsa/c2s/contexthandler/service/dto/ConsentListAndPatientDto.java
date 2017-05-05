package gov.samhsa.c2s.contexthandler.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.Patient;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsentListAndPatientDto {
    private List<Consent> matchingConsents;
    private Patient patient;
}
