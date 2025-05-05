package com.specialwarriors.conal.feat.notificationagreement.domain;

import com.specialwarriors.conal.feat.repo.domain.Repo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationAgreement {

    @Id
    @GeneratedValue
    @Column(name = "notification_agreement_id")
    private Long id;

    @Column(nullable = false)
    private boolean isAgree;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id")
    private Repo repo;


    @Builder
    public NotificationAgreement(boolean isAgree, Repo repo) {
        this.isAgree = isAgree;
        this.repo = repo;
    }

    public static NotificationAgreement of(boolean isAgree) {
        return NotificationAgreement.builder()
            .isAgree(isAgree)
            .build();
    }

    // 연관관계 메서드
    public void setRepo(Repo repo) {
        this.repo = repo;
    }
}
