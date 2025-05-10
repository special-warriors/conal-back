package com.specialwarriors.conal.github_repo.dto.response;

import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import java.time.LocalDate;

public record GithubRepoGetResponse(
    String name,
    String url,
    NotificationAgreement agreement,
    LocalDate endDate,
    String owner,
    String repo
) {

}
