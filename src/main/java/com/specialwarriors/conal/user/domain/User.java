package com.specialwarriors.conal.user.domain;

import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "users")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long githubId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String avatarUrl;

    public User(Long githubId, String username, String avatarUrl) {
        this.githubId = githubId;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    @OneToMany(mappedBy = "user")
    private List<GithubRepo> githubRepos = new ArrayList<>();

    public boolean hasGithubRepo(long repositoryId) {

        return githubRepos.stream().anyMatch(repo -> repo.getId() == repositoryId);
    }

    public void addGithubRepo(GithubRepo githubRepo) {
        githubRepos.add(githubRepo);
    }
}
