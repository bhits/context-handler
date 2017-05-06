package gov.samhsa.c2s.contexthandler.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason ="Multiple patients found for the given MRN" )
public class MultiplePatientsFound extends RuntimeException {
    public MultiplePatientsFound() {
        super();
    }

    public MultiplePatientsFound(String message, Throwable cause,
                           boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MultiplePatientsFound(String message, Throwable cause) {
        super(message, cause);
    }

    public MultiplePatientsFound(String message) {
        super(message);
    }

    public MultiplePatientsFound(Throwable cause) {
        super(cause);
    }
}
