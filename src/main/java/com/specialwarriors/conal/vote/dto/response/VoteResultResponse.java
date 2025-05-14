package com.specialwarriors.conal.vote.dto.response;

import java.util.List;

public record VoteResultResponse(List<VoteResultItem> items) {

    public List<String> emails() {

        return items.stream().map(VoteResultItem::email).toList();
    }

    public int totalVotes() {

        return items.stream().mapToInt(VoteResultItem::votes).sum();
    }
}
