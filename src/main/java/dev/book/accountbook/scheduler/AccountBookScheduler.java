package dev.book.accountbook.scheduler;

import dev.book.accountbook.dto.event.CreateTransEvent;
import dev.book.accountbook.dto.response.AccountBookWeekConsumePerUserResponse;
import dev.book.accountbook.entity.Budget;
import dev.book.accountbook.entity.Codef;
import dev.book.accountbook.entity.TempAccountBook;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.repository.BudgetRepository;
import dev.book.accountbook.repository.CodefRepository;
import dev.book.accountbook.repository.TempAccountBookRepository;
import dev.book.accountbook.service.CodefService;
import dev.book.accountbook.service.StatService;
import dev.book.accountbook.type.Frequency;
import dev.book.accountbook.type.PeriodRange;
import dev.book.achievement.achievement_user.IndividualAchievementStatusService;
import dev.book.achievement.achievement_user.dto.event.SaveConsumeFromBudgetEvent;
import dev.book.achievement.achievement_user.dto.event.SaveConsumeOfWeekEvent;
import dev.book.achievement.achievement_user.dto.event.SuccessBudgetPlanEvent;
import dev.book.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountBookScheduler {
    private final CodefRepository codefRepository;
    private final BudgetRepository budgetRepository;
    private final AccountBookRepository accountBookRepository;
    private final TempAccountBookRepository tempAccountBookRepository;

    private final CodefService codefService;
    private final StatService statService;
    private final IndividualAchievementStatusService individualAchievementStatusService;
    private final ApplicationEventPublisher eventPublisher;

    private final Double LIMIT_SAVE_RATE = 5.0;

    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul") //매월 1일마다 예산 계획 성공 여부
    @Transactional
    public void checkBudgetSuccessfulOrNot() {
        int lastMonth = LocalDate.now().minusMonths(1).getMonth().getValue();
        //저번 달 예산 계획을 세운 사람들
        List<Budget> budgets = budgetRepository.findAllByMonthWithUser(lastMonth);
        getLastMonthBudgetAndCheckSuccessUser(budgets);
        //저번 달 예산 계획을 세우지 않은 사람들
        List<UserEntity> userEntities = budgetRepository.findUsersWithoutBudgetAtMonth(lastMonth);
        userEntities.forEach(
                individualAchievementStatusService::didntHaveBudgetPlan
        );
    }

    private void getLastMonthBudgetAndCheckSuccessUser(List<Budget> budgets) {
        for (Budget budget : budgets) {
            int lastMonthBudget = statService.getTotalConsumeOfLastMonth(budget.getUser().getId());
            if (lastMonthBudget < budget.getBudgetLimit()) {
                // 저번 달 예산 계획과 비교하여 % 이상 절약한 사람들에게 업적 달성
                calcSavedRateAndAchieveBudget(budget.getUser(), budget.getBudgetLimit(), lastMonthBudget);
            }
        }
    }

    private void calcSavedRateAndAchieveBudget(UserEntity user, Integer limit, Integer lastAmount) {
        if (lastAmount != null && lastAmount > 0 && limit > 0) {
            // 예산 계획에 성공한 사람들 횟수 +1
            eventPublisher.publishEvent(new SuccessBudgetPlanEvent(user));
            double savedRate = ((double) (limit - lastAmount) / limit) * 100;
            if (savedRate >= LIMIT_SAVE_RATE)
                eventPublisher.publishEvent(new SaveConsumeFromBudgetEvent(user, savedRate));
        }
    }

    @Scheduled(cron = "0 0 21 * * *")
    public void synchronizeAccount() {
        List<Codef> codefList = codefRepository.findAllCodefWithUserCreatedBeforeToday();

        for (Codef codef : codefList) {
            List<TempAccountBook> savedList = tempAccountBookRepository.saveAll(codefService.getTransactions(codef.getUser()));

            if (!savedList.isEmpty()) {
                eventPublisher.publishEvent(new CreateTransEvent(codef.getUser()));
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * MON", zone = "Asia/Seoul") //매주 월요일마다 주의 소비내역 비교
    @Transactional
    public void checkConsumeOfWeekEveryMonday() {
        PeriodRange periodRange = Frequency.LAST_WEEKLY.calcPeriod();
        //저번주 일주일, 저저번주의 일주일의 소비 내역을 가져와서 비교
        List<AccountBookWeekConsumePerUserResponse> accountBooks = accountBookRepository.findUserAndAmountByConsumeOfWeek(periodRange.currentStart(), periodRange.currentEnd(), periodRange.previousStart(), periodRange.previousEnd());
        accountBooks.forEach(accountBook -> calcSavedRateAndAchieveWeek(accountBook.user(), accountBook.lastWeekAmount(), accountBook.twoWeeksAgoAmount()));
    }

    private void calcSavedRateAndAchieveWeek(UserEntity user, long lastWeekAmount, long twoWeeksAgoAmount) {
        if (twoWeeksAgoAmount > 0 && lastWeekAmount > 0) {
            double savedRate = ((double) (twoWeeksAgoAmount - lastWeekAmount) / twoWeeksAgoAmount) * 100;

            if (savedRate >= LIMIT_SAVE_RATE)
                eventPublisher.publishEvent(new SaveConsumeOfWeekEvent(user, savedRate));
        }
    }
}
