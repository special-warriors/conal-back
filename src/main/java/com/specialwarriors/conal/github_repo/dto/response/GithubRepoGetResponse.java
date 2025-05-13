package com.specialwarriors.conal.github_repo.dto.response;

import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import java.time.LocalDate;
import java.util.List;

public record GithubRepoGetResponse(
    String name,
    String url,
    List<NotificationAgreement> agreement,
    LocalDate endDate,
    String owner,
    String repo
) {

}
