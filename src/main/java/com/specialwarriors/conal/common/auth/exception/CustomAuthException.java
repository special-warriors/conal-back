package com.specialwarriors.conal.common.auth.exception;

import com.specialwarriors.conal.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class CustomAuthException extends RuntimeException {

    private final BaseException error;

    public CustomAuthException(BaseException error) {
        super(error.getMessage());
        this.error = error;
    }

    public HttpStatus getStatus() {
        return error.getStatus();
    }

    public String getErrorCode() {
        return error.getClass().getSimpleName();
    }

    public String getErrorMessage() {
        return error.getMessage();
    }
}
