package dev.book.accountbook.service;

import dev.book.accountbook.dto.request.BudgetRequest;
import dev.book.accountbook.dto.response.BudgetResponse;
import dev.book.accountbook.entity.Budget;
import dev.book.accountbook.exception.accountbook.AccountBookErrorCode;
import dev.book.accountbook.exception.accountbook.AccountBookErrorException;
import dev.book.accountbook.repository.BudgetRepository;
import dev.book.achievement.achievement_user.dto.event.CreateBudgetEvent;
import dev.book.user.entity.UserEntity;
import dev.book.user.exception.UserErrorCode;
import dev.book.user.exception.UserErrorException;
import dev.book.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public BudgetResponse getBudget(Long userId, int month) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
        Budget budget = budgetRepository.findByMonthAndUserId(month, user.getId()).orElseThrow(() -> new AccountBookErrorException(AccountBookErrorCode.NOT_FOUND_BUDGET));

        return budgetRepository.findBudgetWithTotal(budget.getId());
    }

    @Transactional
    public BudgetResponse createBudget(Long userId, BudgetRequest budgetRequest) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
        validateAlreadyExistBudget(userEntity.getId());

        int date = getThisMonth();
        Budget budget = budgetRepository.save(new Budget(budgetRequest.budget(), date, userEntity));

        eventPublisher.publishEvent(new CreateBudgetEvent(userEntity));

        return budgetRepository.findBudgetWithTotal(budget.getId());
    }

    @Transactional
    public BudgetResponse modify(Long userId, Long id, BudgetRequest budgetRequest) {
        Budget budget = findBudgetIdAndUserId(id, userId);
        budget.modifyBudget(budgetRequest.budget());
        budgetRepository.flush();

        return budgetRepository.findBudgetWithTotal(budget.getId());
    }

    @Transactional
    public void deleteBudget(Long userId, Long id) {
        Budget budget = findBudgetIdAndUserId(id, userId);
        budgetRepository.deleteById(budget.getId());
    }

    private void validateAlreadyExistBudget(Long userId) {
        if (budgetRepository.existsByUserId(userId)) {
            throw new AccountBookErrorException(AccountBookErrorCode.DUPLICATE_BUDGET);
        }
    }

    private Budget findBudgetIdAndUserId(Long budgetId, Long userId) {

        return budgetRepository.findByIdAndUserId(budgetId, userId).orElseThrow(() -> new AccountBookErrorException(AccountBookErrorCode.NOT_FOUND_BUDGET));
    }

    private int getThisMonth() {
        return LocalDate.now().getMonthValue();
    }
}
