package dev.book.accountbook.dto.request;

import jakarta.validation.Valid;

import java.util.List;

public record AccountBookSpendListRequest(List<@Valid AccountBookSpendRequest> accountBookSpendRequestList) {
}
