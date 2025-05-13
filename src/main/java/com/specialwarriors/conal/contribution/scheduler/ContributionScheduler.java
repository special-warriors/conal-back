package com.specialwarriors.conal.contribution.scheduler;

import com.specialwarriors.conal.contribution.service.ContributionService;
import com.specialwarriors.conal.contributor.domain.Contributor;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import com.specialwarriors.conal.notification.enums.NotificationType;
import com.specialwarriors.conal.notification.service.NotificationAgreementQuery;
import com.specialwarriors.conal.util.MailUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContributionScheduler {

    private final NotificationAgreementQuery notificationAgreementQuery;
    private final ContributionService contributionService;

    private final MailUtil mailUtil;

    @Scheduled(cron = "0 0 9 ? * FRI")
    public void sendContribution() {
        List<NotificationAgreement> notificationAgreements = notificationAgreementQuery
            .findAllByType(NotificationType.CONTRIBUTION);

        List<GithubRepo> githubRepos = notificationAgreements.stream()
            .map(NotificationAgreement::getGithubRepo)
            .toList();

        for (GithubRepo githubRepo : githubRepos) {
            for (Contributor contributor : githubRepo.getContributors()) {
                mailUtil.sendContributionForm(
                    contributionService.sendEmail(contributor, githubRepo));
            }
        }
    }
}
