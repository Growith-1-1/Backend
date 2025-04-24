package dev.book.accountbook.service;

import dev.book.accountbook.dto.response.AccountBookConsumeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendResponse;
import dev.book.accountbook.dto.response.AccountBookStatResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.type.CategoryType;
import dev.book.accountbook.type.Frequency;
import dev.book.accountbook.type.PeriodRange;
import dev.book.achievement.achievement_user.dto.event.CheckSpendAnalysisEvent;
import dev.book.user.entity.UserEntity;
import dev.book.user.exception.UserErrorCode;
import dev.book.user.exception.UserErrorException;
import dev.book.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatService {
    private final UserRepository userRepository;
    private final AccountBookRepository accountBookRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public List<AccountBookStatResponse> statList(Long userId, Frequency frequency) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
        List<AccountBookStatResponse> statResponses = getStatList(userId, frequency.calcStartDate());

        if (!statResponses.isEmpty()) //거래 내역이 존재하는 소비 분석을 진행 했을 경우
            eventPublisher.publishEvent(new CheckSpendAnalysisEvent(user));

        return statResponses;
    }

    @Transactional
    public List<AccountBookSpendResponse> categoryList(Long userId, Frequency frequency, String category) {
        userRepository.findById(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));

        return getCategoryList(userId, category, frequency.calcStartDate());
    }

    @Transactional
    public AccountBookConsumeResponse consume(Long userId, Frequency frequency) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));

        return getConsume(user, frequency.calcPeriod());
    }

    private List<AccountBookStatResponse> getStatList(Long userId, LocalDate startDate) {

        return accountBookRepository.findTopCategoriesByUserAndPeriod(userId, startDate, LocalDate.now(), CategoryType.SPEND);
    }

    private List<AccountBookSpendResponse> getCategoryList(Long userId, String category, LocalDate starDate) {
        List<AccountBook> responses = accountBookRepository.findByCategory(userId, CategoryType.SPEND, category, starDate, LocalDate.now());

        return responses.stream()
                .map(AccountBookSpendResponse::from)
                .toList();
    }

    private AccountBookConsumeResponse getConsume(UserEntity user, PeriodRange period) {
        Integer thisAmount = accountBookRepository.sumSpending(user.getId(), CategoryType.SPEND, period.currentStart(), period.currentEnd());
        Integer lastAmount = accountBookRepository.sumSpending(user.getId(), CategoryType.SPEND, period.previousStart(), period.previousEnd());

        return new AccountBookConsumeResponse(lastAmount - thisAmount);
    }

    public int getTotalConsumeOfLastMonth(Long userId) {
        PeriodRange periodRange = Frequency.MONTHLY.calcPeriod();

        return accountBookRepository.sumSpending(userId, CategoryType.SPEND, periodRange.previousStart(), periodRange.previousEnd());
    }
}
