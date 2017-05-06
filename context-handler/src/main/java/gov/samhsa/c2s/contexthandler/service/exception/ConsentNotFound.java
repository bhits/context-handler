package gov.samhsa.c2s.contexthandler.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ConsentNotFound extends RuntimeException{
    public ConsentNotFound() {
        super();
    }

    public ConsentNotFound(String message, Throwable cause,
                                     boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConsentNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsentNotFound(String message) {
        super(message);
    }

    public ConsentNotFound(Throwable cause) {
        super(cause);
    }
}
