package com.specialwarriors.conal.common.exception;

import org.springframework.http.HttpStatus;

public interface BaseException {
    HttpStatus getStatus();
    String getMessage();
}