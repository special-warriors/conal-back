package com.specialwarriors.conal.notification.service;

import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import com.specialwarriors.conal.notification.enums.NotificationType;
import com.specialwarriors.conal.notification.exception.NotificationAgreementException;
import com.specialwarriors.conal.notification.repository.NotificationAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationAgreementQuery {

    private final NotificationAgreementRepository notificationAgreementRepository;

    public NotificationAgreement findByRepositoryIdAndType(long repositoryId,
            NotificationType type) {

        return notificationAgreementRepository
                .findAllByRepositoryIdAndNotificationType(repositoryId, type)
                .stream()
                .findFirst()
                .orElseThrow(() -> new GeneralException(
                        NotificationAgreementException.NOTIFICATION_AGREEMENT_NOT_FOUND));
    }
}
