package com.specialwarriors.conal.feat.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
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

    @Builder(access = AccessLevel.PRIVATE)
    private User(Long githubId, String username, String avatarUrl) {
        this.githubId = githubId;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    public static User of(Long githubId, String username, String avatarUrl) {
        return User.builder()
                .githubId(githubId)
                .username(username)
                .avatarUrl(avatarUrl)
                .build();
    }
}
