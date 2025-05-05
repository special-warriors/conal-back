package com.specialwarriors.conal.feat.repo.domain;

import com.specialwarriors.conal.feat.contributor.domain.Contributor;
import com.specialwarriors.conal.feat.notificationagreement.domain.NotificationAgreement;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Repo {

    @Id
    @GeneratedValue
    @Column(name = "repo_id")
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "repo", cascade = CascadeType.ALL)
    @Default
    private List<Contributor> contributors = new ArrayList<>();

    @OneToOne(mappedBy = "repo", cascade = CascadeType.ALL)
    private NotificationAgreement notificationAgreement;

    //연관관계 메서드
    public void addContributors(List<Contributor> contributors) {
        for (Contributor contributor : contributors) {
            this.contributors.add(contributor);
            contributor.setRepo(this);
        }
    }

    public void setNotificationAgreement(NotificationAgreement agreement) {
        this.notificationAgreement = agreement;
        agreement.setRepo(this);
    }

}
