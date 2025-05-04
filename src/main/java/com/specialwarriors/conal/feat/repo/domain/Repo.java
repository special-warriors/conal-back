package com.specialwarriors.conal.feat.repo.domain;

import com.specialwarriors.conal.feat.contributor.domain.Contributor;
import com.specialwarriors.conal.feat.notificationagreement.domain.NotificationAgreement;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
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
    private List<Contributor> contributors = new ArrayList<>();

    @OneToMany(mappedBy = "repo", cascade = CascadeType.ALL)
    private List<NotificationAgreement> notificationAgreements = new ArrayList<>();

    @Builder
    public Repo(String name, String url, LocalDate endDate,
        List<Contributor> contributors, List<NotificationAgreement> notificationAgreements) {

        this.name = name;
        this.url = url;
        this.endDate = endDate;
        this.contributors = contributors;
        this.notificationAgreements = notificationAgreements;
    }

    public static Repo of(String name, String url, LocalDate endDate,
        List<Contributor> contributors, List<NotificationAgreement> notificationAgreements) {

        return Repo.builder()
            .name(name)
            .url(url)
            .endDate(endDate)
            .contributors(contributors)
            .notificationAgreements(notificationAgreements)
            .build();
    }

}
