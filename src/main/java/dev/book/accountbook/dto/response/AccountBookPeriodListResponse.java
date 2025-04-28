package dev.book.accountbook.dto.response;

import java.util.List;

public record AccountBookPeriodListResponse(List<AccountBookPeriodResponse> accountBookPeriodResponse, int totalPage, long totalElement, int number) {
}
