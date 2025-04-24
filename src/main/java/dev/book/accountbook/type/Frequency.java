package dev.book.accountbook.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@RequiredArgsConstructor
public enum Frequency {
    DAILY("daily") {
        @Override
        public LocalDate calcStartDate() {
            return LocalDate.now();
        }

        @Override
        public PeriodRange calcPeriod() {
            LocalDate todayStart = LocalDate.now();
            LocalDate now = LocalDate.now();
            LocalDate yesterdayStart = getYesterdayStart();
            LocalDate yesterdayEnd = getYesterdayEnd();

            return new PeriodRange(todayStart, now, yesterdayStart, yesterdayEnd);
        }
    },
    WEEKLY("weekly") {
        @Override
        public LocalDate calcStartDate() {
            return getStartOfCurrentWeek();
        }

        @Override
        public PeriodRange calcPeriod() {
            LocalDate thisWeekStart = getStartOfCurrentWeek();
            LocalDate now = LocalDate.now();
            LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
            LocalDate lastWeekEnd = thisWeekStart.minusDays(1);

            return new PeriodRange(thisWeekStart, now, lastWeekStart, lastWeekEnd);
        }
    },
    MONTHLY("monthly") {
        @Override
        public LocalDate calcStartDate() {
            return getStartOfCurrentMonth();
        }

        @Override
        public PeriodRange calcPeriod() {
            LocalDate thisMonthStart = getStartOfCurrentMonth();
            LocalDate now = LocalDate.now();
            LocalDate lastMonthStart = thisMonthStart.minusMonths(1);
            LocalDate lastMonthEnd = thisMonthStart.minusDays(1);

            return new PeriodRange(thisMonthStart, now, lastMonthStart, lastMonthEnd);
        }
    },
    YEARLY("yearly") {
        @Override
        public LocalDate calcStartDate() {
            return getStartOfCurrentYear();
        }

        @Override
        public PeriodRange calcPeriod() {
            LocalDate thisYearStart = getStartOfCurrentYear();
            LocalDate now = LocalDate.now();
            LocalDate lastYearStart = thisYearStart.minusYears(1);
            LocalDate lastYearEnd = thisYearStart.minusDays(1);

            return new PeriodRange(thisYearStart, now, lastYearStart, lastYearEnd);
        }
    },
    LAST_WEEKLY("last_weekly") {

        @Override
        public LocalDate calcStartDate() {
            return getStartOfCurrentWeek().minusWeeks(1); //이번주의 -1
        }

        @Override
        public PeriodRange calcPeriod() {
            LocalDate lastWeekStart = calcStartDate();
            LocalDate lastWeekEnd = lastWeekStart.with(DayOfWeek.SUNDAY).with(LocalTime.MAX);
            LocalDate twoWeekAgoStart = lastWeekStart.minusWeeks(1);
            LocalDate twoWeeksAgoEnd = lastWeekEnd.minusWeeks(1);

        return new PeriodRange(lastWeekStart, lastWeekEnd, twoWeekAgoStart, twoWeeksAgoEnd);
    }
    };

    public abstract LocalDate calcStartDate();

    public abstract PeriodRange calcPeriod();


    private static LocalDate getStartOfCurrentWeek() {
        return LocalDate.now()
                .with(DayOfWeek.MONDAY);
    }

    private static LocalDate getStartOfCurrentMonth() {
        return LocalDate.now()
                .withDayOfMonth(1);
    }

    private static LocalDate getYesterdayStart() {
        return LocalDate.now()
                .minusDays(1);
    }

    private static LocalDate getYesterdayEnd() {
        return LocalDate.now()
                .minusDays(1);
    }

    private static LocalDate getStartOfCurrentYear() {
        return LocalDate.now()
                .withDayOfYear(1);
    }

    private final String lower;

    @JsonValue
    public String getLower() {
        return lower;
    }

    @JsonCreator
    public static Frequency from(String value) {
        return Frequency.valueOf(value.toUpperCase());
    }
}
