package com.specialwarriors.conal.github_repo.controller;

import com.specialwarriors.conal.github_repo.dto.request.GithubRepoCreateRequest;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoCreateResponse;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoDeleteResponse;
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

@Controller
@RequestMapping("/users/{userId}/repositories")
@RequiredArgsConstructor
public class GithubRepoController {

    private final GithubRepoService githubRepoService;

    @GetMapping("/new")
    public String showCreateForm(@PathVariable Long userId, Model model) {
        model.addAttribute("repoRequest",
            new GithubRepoCreateRequest(userId, "", "", null, Set.of()));
        model.addAttribute("userId", userId);
        return "repo/form";
    }

    // 저장 (POST)
    @PostMapping
    public String createGitHubRepo(@PathVariable Long userId,
        @ModelAttribute GithubRepoCreateRequest request,
        Model model) {

        GithubRepoCreateResponse response = githubRepoService.createGithubRepo(userId, request);
        return "redirect:/users/" + userId + "/repositories"; // templates/repo/create_result.html
    }

    // 목록 조회 (GET)
    @GetMapping
    public String getGithubRepos(@PathVariable long userId,
        @RequestParam(defaultValue = "0") int page,
        Model model) {

        GithubRepoPageResponse response = githubRepoService.getGithubRepoInfos(userId, page);
        model.addAttribute("repositories", response);
        model.addAttribute("userId", userId);
        return "repo/list"; // templates/repo/list.html
    }

    // 단일 조회 (GET)
    @GetMapping("/{repositoryId}")
    public String getRepositoryId(@PathVariable long userId,
        @PathVariable long repositoryId,
        Model model) {

        GithubRepoGetResponse response = githubRepoService.getGithubRepoInfo(userId, repositoryId);
        model.addAttribute("repoInfo", response);
        return "repo/detail"; // templates/repo/detail.html
    }

    @PostMapping("/{repositoryId}")
    public String deleteResponse(@PathVariable long userId,
        @PathVariable long repositoryId,
        Model model) {
        GithubRepoDeleteResponse response = githubRepoService.deleteRepo(userId, repositoryId);

        return "redirect:/users/" + userId + "/repositories";
    }
}
