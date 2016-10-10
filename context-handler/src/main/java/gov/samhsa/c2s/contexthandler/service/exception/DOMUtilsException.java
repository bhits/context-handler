package gov.samhsa.c2s.contexthandler.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
public class DOMUtilsException extends RuntimeException {

    public DOMUtilsException() {
        super();
    }

    public DOMUtilsException(String message, Throwable cause,
                             boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DOMUtilsException(String message, Throwable cause) {
        super(message, cause);
    }

    public DOMUtilsException(String message) {
        super(message);
    }

    public DOMUtilsException(Throwable cause) {
        super(cause);
    }
}
