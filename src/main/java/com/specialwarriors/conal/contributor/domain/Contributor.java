package com.specialwarriors.conal.contributor.domain;

import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "contributors")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contributor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contributor_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "github_repo_id")
    private GithubRepo githubRepo;

    public Contributor(String email) {
        this.email = email;
    }

    // 연관관계 메서드
    public void setGithubRepo(GithubRepo repo) {
        this.githubRepo = repo;
    }

}
