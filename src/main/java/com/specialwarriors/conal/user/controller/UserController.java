package com.specialwarriors.conal.user.controller;

import com.specialwarriors.conal.common.auth.session.SessionManager;
import com.specialwarriors.conal.github_repo.dto.response.GithubRepoPageResponse;
import com.specialwarriors.conal.github_repo.service.GithubRepoService;
import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final GithubRepoService githubRepoService;
    private final SessionManager sessionManager;

    @GetMapping("/")
    public String index() {

        return "main/index";
    }

    @GetMapping("/home")
    public String home(Model model, @SessionAttribute("userId") Long userId) {

        User user = userService.getUserByUserId(userId);
        GithubRepoPageResponse response = githubRepoService.getGithubRepoInfos(userId, 0);

        model.addAttribute("repositories", response); // 레포지토리 리스트
        model.addAttribute("userId", userId);
        model.addAttribute("username", user.getUsername()); // 필요시 사용

        return "main/home";
    }

    @GetMapping("/login/success")
    public String loginSuccess() {

        return "login/success";
    }

    @GetMapping("/login/failure")
    public String loginFailure() {

        return "login/failure";
    }

    @GetMapping("/mypage")
    public String myPage(Model model, @SessionAttribute("userId") Long userId) {

        User user = userService.getUserByUserId(userId);
        model.addAttribute("avatarUrl", user.getAvatarUrl());
        model.addAttribute("username", user.getUsername());

        return "user/mypage";
    }

    @DeleteMapping("/user")
    public String deleteUser(HttpServletRequest request, @SessionAttribute("userId") Long userId) {

        sessionManager.clearSession(request);
        userService.deleteUser(userId);

        return "user/delete-success";
    }

}
