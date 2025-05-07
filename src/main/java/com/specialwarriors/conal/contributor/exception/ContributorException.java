package com.specialwarriors.conal.contributor.exception;

import com.specialwarriors.conal.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ContributorException implements BaseException {

    MAIL_ACCESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "메일을 보낼 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
