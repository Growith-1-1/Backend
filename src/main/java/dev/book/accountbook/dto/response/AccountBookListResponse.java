package dev.book.accountbook.dto.response;

import java.util.List;

public record AccountBookListResponse(List<AccountBookResponse> accountBookResponseList, int totalPage, long totalElement, int number) {
}
