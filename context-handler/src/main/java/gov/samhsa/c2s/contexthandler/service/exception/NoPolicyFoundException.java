package gov.samhsa.c2s.contexthandler.service.exception;


public class NoPolicyFoundException extends RuntimeException {
    public NoPolicyFoundException() {
    }

    public NoPolicyFoundException(String message) {
        super(message);
    }

    public NoPolicyFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoPolicyFoundException(Throwable cause) {
        super(cause);
    }

    public NoPolicyFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
