package com.specialwarriors.conal.notification.service;

import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.exception.GithubRepoException;
import com.specialwarriors.conal.github_repo.service.GithubRepoQuery;
import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import com.specialwarriors.conal.notification.dto.request.NotificationAgreementUpdateRequest;
import com.specialwarriors.conal.notification.enums.NotificationType;
import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.service.UserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserQuery userQuery;
    private final NotificationAgreementQuery notificationAgreementQuery;
    private final GithubRepoQuery githubRepoQuery;

    @Transactional
    public void updateNotificationAgreement(long userId, long repositoryId,
        NotificationAgreementUpdateRequest request) {

        GithubRepo githubRepo = githubRepoQuery.findByRepositoryId(repositoryId);

        NotificationType notificationType = NotificationType.valueOf(request.type());
        NotificationAgreement notificationAgreement = notificationAgreementQuery
            .findByGithubRepoAndType(githubRepo, notificationType);

        // 사용자가 자신의 github repo에 접근한 것이 맞는 지 검증
        User user = userQuery.findById(userId);
        if (!user.hasGithubRepo(repositoryId)) {
            throw new GeneralException(GithubRepoException.UNAUTHORIZED_GITHUBREPO_ACCESS);
        }

        if (request.isAgree()) {
            notificationAgreement.agree();
        } else {
            notificationAgreement.disagree();
        }
    }
}
