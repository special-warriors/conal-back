package com.specialwarriors.conal.github_repo.dto.response;

import java.time.LocalDate;

public record GithubRepoCreateResponse(
    String name,
    String url,
    LocalDate endDate
) {

}
