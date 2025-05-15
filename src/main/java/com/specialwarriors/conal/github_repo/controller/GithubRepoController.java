package com.specialwarriors.conal.github_repo.controller;

import com.specialwarriors.conal.github.service.GitHubService;
import com.specialwarriors.conal.github_repo.dto.request.GithubRepoCreateRequest;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoCreateResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoGetResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoPageResponse;
import com.specialwarriors.conal.github_repo.service.GithubRepoService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/users/repositories")
@RequiredArgsConstructor
public class GithubRepoController {

    private final GithubRepoService githubRepoService;
    private final GitHubService gitHubService;

    @GetMapping("/new")
    public String showCreateForm(@SessionAttribute Long userId, Model model) {

        model.addAttribute("repoRequest",
            new GithubRepoCreateRequest("", "", null, Set.of()));
        model.addAttribute("userId", userId);

        return "repo/form";
    }

    // 저장 (POST)
    @PostMapping
    public String createGitHubRepo(@SessionAttribute Long userId,
            @ModelAttribute GithubRepoCreateRequest request) {

        GithubRepoCreateResponse response = githubRepoService.createGithubRepo(userId, request);
        gitHubService.updateRepoContribution(response.owner(), response.repo()).subscribe();

        return "redirect:/home";
    }

    // 목록 조회 (GET)
    @GetMapping
    public String getGithubRepos(@SessionAttribute Long userId,
            @RequestParam(defaultValue = "0") int page, Model model) {

        GithubRepoPageResponse response = githubRepoService.getGithubRepoInfos(userId, page);
        model.addAttribute("repositories", response);
        model.addAttribute("userId", userId);

        return "main/home";
    }

    // 단일 조회 (GET)
    @GetMapping("/{repositoryId}")
    public String getRepositoryId(@SessionAttribute Long userId,
            @PathVariable long repositoryId, Model model) {

        GithubRepoGetResponse response = githubRepoService.getGithubRepoInfo(userId, repositoryId);
        model.addAttribute("repoInfo", response);

        return "repo/detail";
    }

    @PostMapping("/{repositoryId}")
    public String deleteRepository(@SessionAttribute Long userId,
            @PathVariable long repositoryId) {

        githubRepoService.deleteRepo(userId, repositoryId);

        return "redirect:/home";
    }
}
