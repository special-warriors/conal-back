package com.specialwarriors.conal.github_repo.dto.response;

import java.time.LocalDate;

public record GithubRepoGetResponse(
    Long userId,
    Long repoId,
    String name,
    String url,
    LocalDate endDate,
    String owner,
    String repo
) {

}
