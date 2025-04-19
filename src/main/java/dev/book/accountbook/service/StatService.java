package dev.book.accountbook.service;

import dev.book.accountbook.dto.response.AccountBookConsumeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendResponse;
import dev.book.accountbook.dto.response.AccountBookStatResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.type.Category;
import dev.book.accountbook.type.CategoryType;
import dev.book.accountbook.type.Frequency;
import dev.book.accountbook.type.PeriodRange;
import dev.book.user.exception.UserErrorCode;
import dev.book.user.exception.UserErrorException;
import dev.book.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatService {
    private final UserRepository userRepository;
    private final AccountBookRepository accountBookRepository;

    @Transactional
    public List<AccountBookStatResponse> statList(Long userId, Frequency frequency) {
        userRepository.findById(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));

        return getStatList(userId, frequency.calcStartDate());
    }

    @Transactional
    public List<AccountBookSpendResponse> categoryList(Long userId, Frequency frequency, Category category) {
        userRepository.findById(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));

        return getCategoryList(userId, category, frequency.calcStartDate());
    }

    @Transactional
    public AccountBookConsumeResponse consume(Long userId, Frequency frequency, Category category) {
        userRepository.findById(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));

        return getConsume(userId, category, frequency.calcPeriod());
    }

    private List<AccountBookStatResponse> getStatList(Long userId, LocalDateTime startDate) {

        return accountBookRepository.findTop3Categories(userId, startDate, LocalDateTime.now(), PageRequest.of(0, 3));
    }

    private List<AccountBookSpendResponse> getCategoryList(Long userId, Category category, LocalDateTime starDate) {
        List<AccountBook> responses = accountBookRepository.findByCategory(userId, CategoryType.SPEND, category, starDate, LocalDateTime.now());

        return responses.stream()
                .map(AccountBookSpendResponse::from)
                .toList();
    }

    private AccountBookConsumeResponse getConsume(Long userId, Category category, PeriodRange period) {
        Integer thisAmount = accountBookRepository.sumSpending(userId, CategoryType.SPEND, category, period.currentStart(), period.currentEnd());
        Integer lastAmount = accountBookRepository.sumSpending(userId, CategoryType.SPEND, category, period.previousStart(), period.previousEnd());

        return new AccountBookConsumeResponse(lastAmount - thisAmount);
    }
}
