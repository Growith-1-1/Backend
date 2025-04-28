package dev.book.accountbook.service;

import dev.book.accountbook.dto.request.BudgetRequest;
import dev.book.accountbook.dto.response.BudgetResponse;
import dev.book.accountbook.entity.Budget;
import dev.book.accountbook.exception.accountbook.AccountBookErrorException;
import dev.book.accountbook.repository.BudgetRepository;
import dev.book.user.entity.UserEntity;
import dev.book.user.exception.UserErrorException;
import dev.book.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class BudgetServiceUnitTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BudgetService budgetService;

    @Test
    @DisplayName("예산을 생성할 수 있다.")
    void createBudget() {
        // given
        UserEntity user = mock(UserEntity.class);
        int budget = 100_000;
        int date = LocalDate.now().getMonthValue();

        Budget budgetEntity = new Budget(budget, date, user);
        BudgetRequest budgetRequest = new BudgetRequest(budget);
        BudgetResponse budgetResponse = new BudgetResponse(1L, budget, 10000);

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(budgetRepository.save(any(Budget.class))).willReturn(budgetEntity);
        given(budgetRepository.existsByUserId(user.getId())).willReturn(false);
        given(budgetRepository.findBudgetWithTotal(budgetEntity.getId())).willReturn(budgetResponse);

        // when
        BudgetResponse response = budgetService.createBudget(user.getId(), budgetRequest);

        // then
        assertThat(response.budget()).isEqualTo(budget);
    }

    @Test
    @DisplayName("예산 생성 중 이미 예산이 등록되어 있다면 예외가 발생한다.")
    void duplicateBudgetException() {
        // given
        UserEntity user = mock(UserEntity.class);
        int budget = 100_000;

        BudgetRequest budgetRequest = new BudgetRequest(budget);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(budgetRepository.existsByUserId(user.getId())).willReturn(true);

        // when
        // then
        assertThatThrownBy(() -> budgetService.createBudget(user.getId(), budgetRequest))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("이미 예산이 등록되어 있습니다.");
    }

    @Test
    @DisplayName("등록된 예산을 불러온다.")
    void getBudget() {
        // given
        Long budgetId = 1L;
        int month = LocalDate.now().getMonthValue();
        UserEntity user = mock(UserEntity.class);
        Budget budget = new Budget(10000, month, user);
        BudgetResponse budgetResponse = new BudgetResponse(budgetId, 10000, 4);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(budgetRepository.findByMonthAndUserId(month, user.getId())).willReturn(Optional.of(budget));
        given(budgetRepository.findBudgetWithTotal(budget.getId())).willReturn(budgetResponse);

        // when
        BudgetResponse response = budgetService.getBudget(user.getId(), LocalDate.now().getMonthValue());

        // then
        assertThat(response.budget()).isEqualTo(budget.getBudgetLimit());
    }

    @Test
    @DisplayName("예산 조회 중 유저가 없을 경우 예외가 발생한다.")
    void notFountUserExceptionByGetBudget() {
        // given
        Long budgetId = 1L;
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> budgetService.getBudget(userId, LocalDate.now().getMonthValue()))
                .isInstanceOf(UserErrorException.class)
                .hasMessage("유저를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("예산 조회 중 찾는 예산이 없을 경우 예외가 발생한다.")
    void notFountBudgetExceptionByGetBudget() {
        // given
        Long budgetId = 1L;
        Long userId = 1L;
        UserEntity user = mock(UserEntity.class);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        // then
        assertThatThrownBy(() -> budgetService.getBudget(userId, LocalDate.now().getMonthValue()))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 예산입니다.");
    }

    @Test
    @DisplayName("예산을 수정할 수 있다.")
    void modifyBudget() {
        // given
        Long budgetId = 1L;
        UserEntity user = mock(UserEntity.class);
        Budget budget = new Budget(10000, 4, user);

        BudgetRequest modifyRequest = new BudgetRequest(100000);
        BudgetResponse modifyResponse = new BudgetResponse(budgetId, 100000, 1000);

        given(budgetRepository.findByIdAndUserId(budgetId, user.getId())).willReturn(Optional.of(budget));
        given(budgetRepository.findBudgetWithTotal(budget.getId())).willReturn(modifyResponse);

        // when
        BudgetResponse response = budgetService.modify(user.getId(), budgetId, modifyRequest);

        // then
        assertThat(response.budget()).isEqualTo(modifyRequest.budget());
    }

    @Test
    @DisplayName("예산 수정 중 찾는 예산이 없을 경우 예외가 발생한다.")
    void notFoundBudgetByModifyBudget() {
        // given
        Long budgetId = 1L;
        Long userId = 1L;
        given(budgetRepository.findByIdAndUserId(budgetId, userId)).willReturn(Optional.empty());
        BudgetRequest request = new BudgetRequest(10000);

        // when
        // then
        assertThatThrownBy(() -> budgetService.modify(budgetId, userId, request))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 예산입니다.");
    }

    @Test
    @DisplayName("예산 삭제 중 찾는 예산이 없을 경우 예외가 발생한다.")
    void notFoundBudgetByDeleteBudget() {
        // given
        Long budgetId = 1L;
        Long userId = 1L;
        given(budgetRepository.findByIdAndUserId(budgetId, userId)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> budgetService.deleteBudget(budgetId, userId))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 예산입니다.");
    }
}
