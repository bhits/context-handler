package gov.samhsa.c2s.contexthandler.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class PolicyCombiningAlgIdNotFoundException extends RuntimeException {

    public PolicyCombiningAlgIdNotFoundException() {
        super();
    }

    public PolicyCombiningAlgIdNotFoundException(String message) {
        super(message);
    }

    public PolicyCombiningAlgIdNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PolicyCombiningAlgIdNotFoundException(String message,
                                                 Throwable cause, boolean enableSuppression,
                                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PolicyCombiningAlgIdNotFoundException(Throwable cause) {
        super(cause);
    }

}
