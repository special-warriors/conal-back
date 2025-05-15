package com.specialwarriors.conal.user.service;

import com.specialwarriors.conal.common.auth.oauth.GithubOAuth2WebClient;
import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.github_repo.domain.GithubRepo;
import com.specialwarriors.conal.github_repo.repository.GithubRepoRepository;
import com.specialwarriors.conal.notification.repository.NotificationAgreementRepository;
import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.exception.UserException;
import com.specialwarriors.conal.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GithubRepoRepository githubRepoRepository;
    private final NotificationAgreementRepository notificationAgreementRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final GithubOAuth2WebClient githubOAuth2WebClient;

    public User findById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND));
    }

    public void deleteById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND));

        List<GithubRepo> repos = user.getGithubRepos();
        for (GithubRepo repo : repos) {
            notificationAgreementRepository.deleteByGithubRepoId(repo.getId());
        }

        githubRepoRepository.deleteAll(repos);

        String oauthAccessToken = redisTemplate.opsForValue()
                .get("github:token:" + user.getGithubId());

        githubOAuth2WebClient.unlink(oauthAccessToken);
        redisTemplate.unlink("github:token:" + user.getGithubId());

        userRepository.deleteById(userId);
    }
}
