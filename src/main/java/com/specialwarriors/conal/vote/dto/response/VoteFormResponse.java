package com.specialwarriors.conal.vote.dto.response;

import java.util.List;

public record VoteFormResponse(long repoId, String userToken, String email,
                               List<String> voteTargetEmails) {

}
