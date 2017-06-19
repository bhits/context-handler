package gov.samhsa.c2s.contexthandler.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class PcmClientInterfaceException extends RuntimeException {

    public PcmClientInterfaceException() {
        super();
    }

    public PcmClientInterfaceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PcmClientInterfaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PcmClientInterfaceException(String message) {
        super(message);
    }

    public PcmClientInterfaceException(Throwable cause) {
        super(cause);
    }
}
