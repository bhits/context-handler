package gov.samhsa.c2s.contexthandler.service.exception;


public class PolicyProviderException extends RuntimeException {

    public PolicyProviderException() {
    }

    public PolicyProviderException(String message) {
        super(message);
    }

    public PolicyProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public PolicyProviderException(Throwable cause) {
        super(cause);
    }

    public PolicyProviderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
