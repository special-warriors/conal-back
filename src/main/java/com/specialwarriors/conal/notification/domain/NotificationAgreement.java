package com.specialwarriors.conal.notification.domain;

import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.notification.converter.NotificationTypeConverter;
import com.specialwarriors.conal.notification.enums.NotificationType;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "notification_agreements")
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long repositoryId;

    private boolean isAgree;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "github_repo_id")
    private GithubRepo githubRepo;

    @Convert(converter = NotificationTypeConverter.class)
    private NotificationType notificationType;

    public void agree() {
        this.isAgree = true;
    }

    public void disagree() {
        this.isAgree = false;
    }

    public void setGitHubRepo(GithubRepo githubRepo) {
        this.githubRepo = githubRepo;
    }

    public static NotificationAgreement of(NotificationType notificationType) {
        return NotificationAgreement.builder()
            .notificationType(notificationType)
            .build();
    }
}
