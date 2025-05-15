package com.specialwarriors.conal.vote.exception;

import com.specialwarriors.conal.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VoteException implements BaseException {

    VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 투표입니다."),
    UNAUTHORIZED_VOTE_ACCESS(HttpStatus.UNAUTHORIZED, "투표 접근 권한이 없습니다"),
    ALREADY_VOTED(HttpStatus.BAD_REQUEST, "이미 투표했습니다.");

    private final HttpStatus status;
    private final String message;
}
