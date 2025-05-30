package com.specialwarriors.conal.notification.domain;

import com.specialwarriors.conal.notification.converter.NotificationTypeConverter;
import com.specialwarriors.conal.notification.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "notification_agreements")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isAgree;

    @Column(name = "github_repo_id")
    private long githubRepoId;

    @Convert(converter = NotificationTypeConverter.class)
    private NotificationType notificationType;

    public void agree() {
        this.isAgree = true;
    }

    public void disagree() {
        this.isAgree = false;
    }

    public void setGitHubRepoId(Long githubRepoId) {
        this.githubRepoId = githubRepoId;
    }

    public NotificationAgreement(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
}
