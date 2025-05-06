package com.specialwarriors.conal.common.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public class GeneralException extends RuntimeException {

    private final BaseException exception;

    public HttpStatus getStatus() {

        return exception.getStatus();
    }

    public String getMessage() {

        return exception.getMessage();
    }
}
