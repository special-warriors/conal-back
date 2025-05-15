package com.specialwarriors.conal.vote.scheduler;

import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import com.specialwarriors.conal.notification.enums.NotificationType;
import com.specialwarriors.conal.notification.service.NotificationAgreementQuery;
import com.specialwarriors.conal.util.MailUtil;
import com.specialwarriors.conal.vote.dto.response.VoteResultResponse;
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
    private final MailUtil mailUtil;

    @Scheduled(cron = "0 0 9 ? * FRI")
    public void openWeeklyVote() {
        List<NotificationAgreement> notificationAgreements = notificationAgreementQuery
            .findAllByType(NotificationType.VOTE);

        List<Long> repositoryIds = extractGithubRepoIdsFrom(notificationAgreements);

        repositoryIds.forEach(voteService::openVote);
    }

    @Scheduled(cron = "0 0 18 ? * FRI")
    public void sendWeeklyVoteForm() {
        List<NotificationAgreement> notificationAgreements = notificationAgreementQuery
            .findAllByType(NotificationType.VOTE);

        List<Long> repositoryIds = extractGithubRepoIdsFrom(notificationAgreements);

        for (long repositoryId : repositoryIds) {
            voteService.getVoteFormResponse(repositoryId).forEach(mailUtil::sendVoteForm);
        }
    }

    @Scheduled(cron = "0 0 9 ? * MON")
    public void sendWeeklyVoteResult() {
        List<NotificationAgreement> notificationAgreements = notificationAgreementQuery
            .findAllByType(NotificationType.VOTE);

        List<Long> repositoryIds = extractGithubRepoIdsFrom(notificationAgreements);
        List<VoteResultResponse> voteResults = repositoryIds.stream()
            .map(voteService::getVoteResult)
            .toList();

        for (VoteResultResponse voteResult : voteResults) {
            List<String> emails = voteResult.emails();
            emails.forEach(email -> mailUtil.sendVoteResult(email, voteResult));
        }
    }


    private List<Long> extractGithubRepoIdsFrom(
        List<NotificationAgreement> notificationAgreements) {

        return notificationAgreements.stream()
            .map(NotificationAgreement::getGithubRepoId)
            .distinct()
            .toList();
    }
}
