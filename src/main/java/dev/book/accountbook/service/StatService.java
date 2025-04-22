package dev.book.accountbook.service;

import dev.book.accountbook.dto.response.AccountBookConsumeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendResponse;
import dev.book.accountbook.dto.response.AccountBookStatResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.type.CategoryType;
import dev.book.accountbook.type.Frequency;
import dev.book.accountbook.type.PeriodRange;
import dev.book.achievement.achievement_user.IndividualAchievementStatusService;
import dev.book.global.entity.Category;
import dev.book.user.entity.UserEntity;
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

    private final IndividualAchievementStatusService individualAchievementStatusService;

    @Transactional
    public List<AccountBookStatResponse> statList(Long userId, Frequency frequency) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
        individualAchievementStatusService.pluCheckSpendAnalysis(user);

        return getStatList(userId, frequency.calcStartDate());
    }

    @Transactional
    public List<AccountBookSpendResponse> categoryList(Long userId, Frequency frequency, String category) {
        userRepository.findById(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));

        return getCategoryList(userId, category, frequency.calcStartDate());
    }

    @Transactional
    public AccountBookConsumeResponse consume(Long userId, Frequency frequency) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));

        return getConsume(user, frequency);
    }

    private List<AccountBookStatResponse> getStatList(Long userId, LocalDateTime startDate) {

        return accountBookRepository.findTopCategoriesByUserAndPeriod(userId, startDate, LocalDateTime.now(), PageRequest.of(0, 3));
    }

    private List<AccountBookSpendResponse> getCategoryList(Long userId, String category, LocalDateTime starDate) {
        List<AccountBook> responses = accountBookRepository.findByCategory(userId, CategoryType.SPEND, category, starDate, LocalDateTime.now());

        return responses.stream()
                .map(AccountBookSpendResponse::from)
                .toList();
    }

    private AccountBookConsumeResponse getConsume(UserEntity user, Frequency frequency) {
        PeriodRange period = frequency.calcPeriod();
        Integer thisAmount = accountBookRepository.sumSpending(user.getId(), CategoryType.SPEND, period.currentStart(), period.currentEnd());
        Integer lastAmount = accountBookRepository.sumSpending(user.getId(), CategoryType.SPEND, period.previousStart(), period.previousEnd());

        if (Frequency.WEEKLY.equals(frequency))
            calcSavedRateAndAchieve(user, thisAmount, lastAmount);

        return new AccountBookConsumeResponse(lastAmount - thisAmount);
    }

    private void calcSavedRateAndAchieve(UserEntity user, Integer thisAmount, Integer lastAmount) {
        if (lastAmount != null && lastAmount > 0) {
            double savedRate = ((double) (lastAmount - thisAmount) / lastAmount) * 100;
            individualAchievementStatusService.achieveSaveAccomplishmentOfWeek(user, savedRate);
        }
    }

    public AccountBookConsumeResponse getCategoryTotalConsume(Long userId, List<Category> category, LocalDateTime starDate, LocalDateTime endDate) {
//        List<AccountBook> responses = accountBookRepository.findByCategory(userId, CategoryType.SPEND, category, starDate, LocalDateTime.now());
        return null;
        //todo 카테고리 리스트의 총 소비양
    }

    public int getTotalConsumeOfLastMonth(Long userId) {
        PeriodRange periodRange = Frequency.MONTHLY.calcPeriod();

        return accountBookRepository.sumSpending(userId, CategoryType.SPEND, periodRange.previousStart(), periodRange.previousEnd());
    }
}
