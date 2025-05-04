package com.specialwarriors.conal.feat.notificationagreement.service;

import com.specialwarriors.conal.feat.notificationagreement.repository.NotificationAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationAgreementService {

    private final NotificationAgreementRepository notificationAgreementRepository;

}
