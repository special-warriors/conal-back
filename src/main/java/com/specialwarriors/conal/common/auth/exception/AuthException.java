package com.specialwarriors.conal.common.auth.exception;

import com.specialwarriors.conal.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthException implements BaseException {

    OAUTH_PROVIDER_ERROR(HttpStatus.UNAUTHORIZED, "인증 제공자 오류"),
    OAUTH_USER_CANCELLED(HttpStatus.UNAUTHORIZED, "사용자가 인증을 취소함"),
    USER_MAPPING_FAILED(HttpStatus.UNAUTHORIZED, "OAuth 사용자 정보를 처리할 수 없음"),
    INVALID_REDIRECT_URI(HttpStatus.UNAUTHORIZED, "리다이렉트 URI가 유효하지 않음"),
    DENIED_AUTHENTICATION(HttpStatus.UNAUTHORIZED, "인증이 거부되었습니다"),
    EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "토큰 값이 비어있습니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");

    private final HttpStatus status;
    private final String message;
}
