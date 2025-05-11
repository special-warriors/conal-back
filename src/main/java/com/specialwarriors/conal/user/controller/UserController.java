package com.specialwarriors.conal.user.controller;

import com.specialwarriors.conal.user.domain.User;
import com.specialwarriors.conal.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/")
    public String index() {

        return "main/index";
    }

    @GetMapping("/home")
    public String home() {

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

}
