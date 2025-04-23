package dev.book.accountbook.dto.response;

import java.util.List;

public record AccountBookMonthResponse(int day, int spendTotal, int incomeTotal, List<AccountBookPeriodResponse> dayList) {
}
