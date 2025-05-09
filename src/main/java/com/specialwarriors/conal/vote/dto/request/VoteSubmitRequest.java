package com.specialwarriors.conal.vote.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record VoteSubmitRequest(long repoId,
                                @NotEmpty(message = "userToken은 필수값입니다.") String userToken,
                                @NotEmpty(message = "투표한 이메일은 필수값입니다.") String votedEmail) {

}
