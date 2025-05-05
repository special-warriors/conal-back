package com.specialwarriors.conal.feat.contributor.domain;

import com.specialwarriors.conal.feat.repo.domain.Repo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Contributor {

    @Id
    @GeneratedValue
    @Column(name = "contributor_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id")
    private Repo repo;

    public static Contributor of(String email) {
        return Contributor.builder()
            .email(email)
            .build();
    }

    // 연관관계 메서드
    public void setRepo(Repo repo) {
        this.repo = repo;
    }

}
