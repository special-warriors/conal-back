package com.specialwarriors.conal.feat.notificationagreement.domain;

import com.specialwarriors.conal.feat.repo.domain.Repo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationAgreement {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private boolean isAgree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id")
    private Repo repo;


    @Builder
    public NotificationAgreement(boolean isAgree, Repo repo) {
        this.isAgree = isAgree;
        this.repo = repo;
    }

    public static NotificationAgreement of(boolean isAgree, Repo repo) {
        return NotificationAgreement.builder()
            .isAgree(isAgree)
            .repo(repo)
            .build();
    }
}
