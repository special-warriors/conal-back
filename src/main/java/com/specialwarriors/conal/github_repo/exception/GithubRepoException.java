package com.specialwarriors.conal.github_repo.exception;

import com.specialwarriors.conal.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GithubRepoException implements BaseException {

    UNAUTHORIZED_GITHUB_REPO_ACCESS(HttpStatus.FORBIDDEN, "리포지토리 접근 권한이 없습니다"),
    GITHUB_REPO_NOT_FOUND(HttpStatus.NOT_FOUND, "깃허브 리포지토리를 찾을 수 없습니다"),
    CONTRIBUTOR_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "기여자 이메일이 없습니다"),
    INVALID_URL(HttpStatus.BAD_REQUEST, "잘못된 URL 입니다.");

    private final HttpStatus status;
    private final String message;
}
