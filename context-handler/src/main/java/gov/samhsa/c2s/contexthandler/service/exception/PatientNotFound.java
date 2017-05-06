package gov.samhsa.c2s.contexthandler.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason ="Patient not found for the given MRN")
public class PatientNotFound extends RuntimeException {
    public PatientNotFound() {
        super();
    }

    public PatientNotFound(String message, Throwable cause,
                           boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PatientNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public PatientNotFound(String message) {
        super(message);
    }

    public PatientNotFound(Throwable cause) {
        super(cause);
    }
}
