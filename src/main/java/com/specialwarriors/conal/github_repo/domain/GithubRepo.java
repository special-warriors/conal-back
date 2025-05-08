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
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubRepo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "github_repo_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "githubRepo", cascade = CascadeType.ALL)

    private List<Contributor> contributors = new ArrayList<>();

    @OneToOne(mappedBy = "githubRepo", cascade = CascadeType.ALL)
    private NotificationAgreement notificationAgreement;

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

    public void setNotificationAgreement(NotificationAgreement agreement) {
        this.notificationAgreement = agreement;
        agreement.setGitHubRepo(this);
    }

    public void setUser(User user) {
        this.user = user;
        user.addGithubRepo(this);
    }

}
