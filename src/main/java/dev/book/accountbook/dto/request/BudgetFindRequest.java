package dev.book.accountbook.dto.request;

import java.time.LocalDate;

public record BudgetFindRequest(LocalDate month) {
}
