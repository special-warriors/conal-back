package com.specialwarriors.conal.common.auth.exception;

import com.specialwarriors.conal.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthException implements BaseException {

    OAUTH_PROVIDER_ERROR(HttpStatus.UNAUTHORIZED, "인증 제공자 오류"),
    OAUTH_USER_CANCELED(HttpStatus.UNAUTHORIZED, "사용자가 인증을 취소함"),
    INVALID_REDIRECT_URI(HttpStatus.UNAUTHORIZED, "리다이렉트 URI가 유효하지 않음"),
    EMPTY_OAUTH_TOKEN(HttpStatus.UNAUTHORIZED, "ACCESS TOKEN이 비어있음");

    private final HttpStatus status;
    private final String message;
}
