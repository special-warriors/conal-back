package com.specialwarriors.conal.github_repo.dto.response;

import java.time.LocalDate;
import java.util.List;

public record GithubRepoPageResponse(
    List<GithubRepoSummary> repos,
    int currentPage,
    int totalPages,
    long totalElements
) {

    public record GithubRepoSummary(
        Long id,
        String name,
        String url,
        LocalDate endDate
    ) {

    }
}
