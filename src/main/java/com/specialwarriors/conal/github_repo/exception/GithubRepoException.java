package com.specialwarriors.conal.github_repo.exception;

import com.specialwarriors.conal.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GithubRepoException implements BaseException {

    UNAUTHORIZED_REPO_ACCESS(HttpStatus.FORBIDDEN, "리포지토리 접근 권한이 없습니다"),
    NOT_FOUND_REPONAME(HttpStatus.NOT_FOUND, "깃허브 리포지토리 이름을 찾을 수 없습니다"),
    NOT_FOUND_REPO(HttpStatus.NOT_FOUND, "깃허브 리포지토리를 찾을 수 없습니다"),
    NOT_FOUND_EMAIL(HttpStatus.NOT_FOUND, "기여자 이메일이 없습니다"),
    EXCEED_EMAIL(HttpStatus.BAD_REQUEST, "이메일은 5개까지 등록할 수 있습니다"),
    INVALID_URL(HttpStatus.BAD_REQUEST, "잘못된 URL 입니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "잘못된 이메일 입니다"),
    INVALID_DURATION(HttpStatus.NOT_FOUND, "종료일이 존재하지 않습니다");

    private final HttpStatus status;
    private final String message;
}
