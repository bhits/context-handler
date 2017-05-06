package gov.samhsa.c2s.contexthandler.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class FhirConsentInvalidException extends RuntimeException {

    public FhirConsentInvalidException() {
        super();
    }

    public FhirConsentInvalidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public FhirConsentInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public FhirConsentInvalidException(String message) {
        super(message);
    }

    public FhirConsentInvalidException(Throwable cause) {
        super(cause);
    }
}
