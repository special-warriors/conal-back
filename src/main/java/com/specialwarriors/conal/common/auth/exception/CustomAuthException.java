package com.specialwarriors.conal.common.auth.exception;

import com.specialwarriors.conal.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public class CustomAuthException extends RuntimeException {

    private final BaseException exception;

    public HttpStatus getStatus() {

        return exception.getStatus();
    }

    public String getMessage() {

        return exception.getMessage();
    }
}
