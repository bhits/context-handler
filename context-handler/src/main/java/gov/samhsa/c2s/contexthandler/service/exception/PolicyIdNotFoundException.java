package gov.samhsa.c2s.contexthandler.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
public class PolicyIdNotFoundException extends RuntimeException {

    public PolicyIdNotFoundException() {
        super();
    }

    public PolicyIdNotFoundException(String message, Throwable cause,
                                     boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PolicyIdNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PolicyIdNotFoundException(String message) {
        super(message);
    }

    public PolicyIdNotFoundException(Throwable cause) {
        super(cause);
    }
}
