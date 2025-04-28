package dev.book.accountbook.dto.response;

import java.util.List;

public record AccountBookIncomeListResponse(List<AccountBookIncomeResponse> accountBookIncomeResponseList, int totalPage, long totalElement, int number) {
}
