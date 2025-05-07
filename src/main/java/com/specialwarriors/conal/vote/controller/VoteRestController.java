package com.specialwarriors.conal.vote.controller;

import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.vote.dto.request.VoteSaveRequest;
import com.specialwarriors.conal.vote.exception.VoteException;
import com.specialwarriors.conal.vote.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VoteRestController {

    private final VoteService voteService;

    @PostMapping("/repositories/{repositoryId}/votes/{voteUuid}")
    public ResponseEntity<Void> saveVote(@PathVariable long repositoryId,
            @PathVariable String voteUuid,
            @RequestParam String userToken,
            @RequestBody @Valid VoteSaveRequest request) {

        boolean voteSaveStatus = voteService.saveVoteRequest(repositoryId, voteUuid, userToken,
                request);
        if (!voteSaveStatus) {
            throw new GeneralException(VoteException.ALREADY_VOTED);
        }
        voteService.saveVoteResult(repositoryId, voteUuid, request);

        return ResponseEntity.ok().build();
    }
}
