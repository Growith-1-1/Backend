package dev.book.accountbook.type;

import java.time.LocalDate;

public record PeriodRange(LocalDate currentStart, LocalDate currentEnd, LocalDate previousStart, LocalDate previousEnd) {
}
