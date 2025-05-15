package com.specialwarriors.conal.notification.service;

import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.service.GithubRepoQuery;
import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import com.specialwarriors.conal.notification.enums.NotificationType;
import com.specialwarriors.conal.notification.exception.NotificationAgreementException;
import com.specialwarriors.conal.notification.repository.NotificationAgreementRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationAgreementQuery {

    private final GithubRepoQuery githubRepoQuery;
    private final NotificationAgreementRepository notificationAgreementRepository;

    public NotificationAgreement findByGithubRepoAndType(GithubRepo githubRepo,
        NotificationType type) {

        return notificationAgreementRepository
            .findAllByGithubRepoIdAndNotificationType(githubRepo.getId(), type)
            .stream()
            .findFirst()
            .orElseThrow(() -> new GeneralException(
                NotificationAgreementException.NOTIFICATION_AGREEMENT_NOT_FOUND));
    }

    public List<NotificationAgreement> findAllByType(NotificationType type) {

        return notificationAgreementRepository.findAllByNotificationType(type);
    }
}
