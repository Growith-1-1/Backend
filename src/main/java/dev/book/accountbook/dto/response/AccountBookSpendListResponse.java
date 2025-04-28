package dev.book.accountbook.dto.response;

import java.util.List;

public record AccountBookSpendListResponse(List<AccountBookSpendResponse> accountBookSpendResponseList, int totalPage, long totalElement, int number) {
}
