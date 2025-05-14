package com.specialwarriors.conal.vote.controller;

import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.vote.dto.request.VoteSubmitRequest;
import com.specialwarriors.conal.vote.exception.VoteException;
import com.specialwarriors.conal.vote.service.VoteService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @GetMapping("/repositories/{repoId}/vote-form")
    public String getVoteForm(@PathVariable long repoId,
            @RequestParam String userToken,
            Model model) {

        model.addAttribute("repositoryId", repoId);
        model.addAttribute("userToken", userToken);

        List<String> emails = voteService.findVoteTargetEmails(repoId, userToken);
        model.addAttribute("emails", emails);

        return "vote/form";
    }

    @PostMapping("/repositories/{repoId}/votes")
    public String submitVote(@PathVariable long repoId,
            @ModelAttribute @Valid VoteSubmitRequest request) {
        boolean result = voteService.saveVoteRequest(repoId, request);

        if (!result) {
            throw new GeneralException(VoteException.ALREADY_VOTED);
        }

        voteService.saveVoteResult(repoId, request);

        return "vote/complete";
    }
}
