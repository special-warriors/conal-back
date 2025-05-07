package com.specialwarriors.conal.notification.exception;

import com.specialwarriors.conal.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationAgreementException implements BaseException {

    NOTIFICATION_AGREEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "알림 동의 내역을 찾을 수 없습니다");

    private final HttpStatus status;
    private final String message;
}
