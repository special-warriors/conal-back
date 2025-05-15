package com.specialwarriors.conal.notification.controller;

import com.specialwarriors.conal.notification.dto.request.NotificationAgreementUpdateRequest;
import com.specialwarriors.conal.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationRestController {

    private final NotificationService notificationService;

    @PostMapping("/users/{userId}/repositories/{repositoryId}/notifications")
    public ResponseEntity<Void> updateNotificationAgreement(@PathVariable long userId,
            @PathVariable long repositoryId,
            @RequestBody NotificationAgreementUpdateRequest request) {

        notificationService.updateNotificationAgreement(userId, repositoryId, request);

        return ResponseEntity.ok().build();
    }
}
