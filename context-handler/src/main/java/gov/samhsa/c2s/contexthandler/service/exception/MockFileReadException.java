package gov.samhsa.c2s.contexthandler.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class MockFileReadException extends RuntimeException {
    public MockFileReadException(Throwable cause){
        super(cause);
    }
}
