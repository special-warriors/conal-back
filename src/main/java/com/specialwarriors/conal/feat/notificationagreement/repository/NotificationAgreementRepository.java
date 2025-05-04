package com.specialwarriors.conal.feat.notificationagreement.repository;

import com.specialwarriors.conal.feat.notificationagreement.domain.NotificationAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationAgreementRepository extends
    JpaRepository<NotificationAgreement, Long> {

}
