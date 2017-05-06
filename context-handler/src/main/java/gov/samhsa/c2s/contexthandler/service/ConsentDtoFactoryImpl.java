package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.common.consentgen.ConsentDto;
import gov.samhsa.c2s.common.consentgen.ConsentDtoFactory;

public class ConsentDtoFactoryImpl implements ConsentDtoFactory {

    public ConsentDtoFactoryImpl(){
        super();
    }

    @Override
    public ConsentDto createConsentDto(long consentId) {
        return null;
    }

    @Override
    public ConsentDto createConsentDto(Object obj) {
        return (ConsentDto) obj;
    }
}
