package com.specialwarriors.conal.vote.scheduler;

import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import com.specialwarriors.conal.notification.enums.NotificationType;
import com.specialwarriors.conal.notification.service.NotificationAgreementQuery;
import com.specialwarriors.conal.vote.service.VoteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VoteScheduler {

    private final NotificationAgreementQuery notificationAgreementQuery;
    private final VoteService voteService;

    @Scheduled(cron = "0 0 9 ? * FRI")
    public void openWeeklyVote() {
        List<NotificationAgreement> notificationAgreements = notificationAgreementQuery
                .findAllByType(NotificationType.VOTE);
        List<Long> repositoryIds = notificationAgreements.stream()
                .map(notificationAgreement ->
                        notificationAgreement.getGithubRepo().getId())
                .toList();

        repositoryIds.forEach(voteService::openVote);
    }
}
