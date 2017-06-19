package gov.samhsa.c2s.contexthandler.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoConsentFoundException extends RuntimeException{
    public NoConsentFoundException() {
        super();
    }

    public NoConsentFoundException(String message, Throwable cause,
                                   boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NoConsentFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoConsentFoundException(String message) {
        super(message);
    }

    public NoConsentFoundException(Throwable cause) {
        super(cause);
    }
}
