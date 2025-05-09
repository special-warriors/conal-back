package com.specialwarriors.conal.github_repo.dto.request;

import java.time.LocalDate;
import java.util.Set;

public record GithubRepoCreateRequest(
    String name,
    String url,
    LocalDate endDate,
    Set<String> emails
) {

}
