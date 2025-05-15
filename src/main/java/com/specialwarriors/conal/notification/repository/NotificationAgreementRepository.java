package com.specialwarriors.conal.notification.repository;

import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import com.specialwarriors.conal.notification.enums.NotificationType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationAgreementRepository extends
        JpaRepository<NotificationAgreement, Long> {

    List<NotificationAgreement> findAllByGithubRepoIdAndNotificationType(Long githubRepoId,
            NotificationType notificationType);

    List<NotificationAgreement> findAllByNotificationType(NotificationType notificationType);

    void deleteByGithubRepoId(long githubRepoId);
}
