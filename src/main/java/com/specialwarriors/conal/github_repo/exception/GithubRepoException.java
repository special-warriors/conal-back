package com.specialwarriors.conal.github_repo.exception;

import com.specialwarriors.conal.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GithubRepoException implements BaseException {

    UNAUTHORIZED_REPO_ACCESS(HttpStatus.BAD_REQUEST, "사용자는 자신의 repo에만 접근할 수 있습니다.");

    private final HttpStatus status;
    private final String message;
}
