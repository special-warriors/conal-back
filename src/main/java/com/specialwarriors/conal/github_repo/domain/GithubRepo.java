package com.specialwarriors.conal.github_repo.domain;

import com.specialwarriors.conal.contributor.domain.Contributor;
import com.specialwarriors.conal.notification.domain.NotificationAgreement;
import com.specialwarriors.conal.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "github_repos")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class GithubRepo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "githubRepo", cascade = CascadeType.ALL)
    private List<Contributor> contributors = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public GithubRepo(String name, String url, LocalDate endDate) {
        this.name = name;
        this.url = url;
        this.endDate = endDate;
        this.contributors = new ArrayList<>();
    }

    //연관관계 메서드
    public void addContributors(List<Contributor> contributors) {
        for (Contributor contributor : contributors) {
            this.contributors.add(contributor);
            contributor.setGithubRepo(this);
        }
    }

    public void assignRepoIdToNotificationAgreements(
        List<NotificationAgreement> notificationAgreements) {

        notificationAgreements.forEach(agreement -> agreement.setGitHubRepoId(this.getId()));
    }


    public void setUser(User user) {
        this.user = user;
        user.addGithubRepo(this);
    }

}
