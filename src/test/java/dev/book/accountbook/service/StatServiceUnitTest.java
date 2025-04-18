package dev.book.accountbook.service;

import dev.book.accountbook.dto.response.AccountBookConsumeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendResponse;
import dev.book.accountbook.dto.response.AccountBookStatResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.exception.accountbook.AccountBookErrorException;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.type.Category;
import dev.book.accountbook.type.CategoryType;
import dev.book.accountbook.type.Frequency;
import dev.book.accountbook.type.PeriodRange;
import dev.book.user.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class StatServiceUnitTest {
    @Mock
    private AccountBookRepository accountBookRepository;
    @InjectMocks
    private StatService statService;

    @Test
    @DisplayName("주기를 기준으로 소비내역을 가져온다.")
    void statList() {
        // given
        Frequency frequency = Frequency.MONTHLY;
        LocalDateTime start = frequency.calcStartDate();
        List<AccountBookStatResponse> mockResponses = List.of(
                new AccountBookStatResponse(Category.FOOD, 10000L),
                new AccountBookStatResponse(Category.CAFE_SNACK, 8000L),
                new AccountBookStatResponse(Category.SHOPPING, 5000L)
        );

        given(accountBookRepository.findTop3Categories(anyLong(), eq(start), any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(mockResponses);

        // when
        List<AccountBookStatResponse> result = statService.statList(1L, frequency);

        // then
        assertThat(3).isEqualTo(result.size());
    }

    @Test
    @DisplayName("카테고리를 기준으로 소비내역을 가져온다.")
    void categoryList() {
        // given
        UserEntity user = mock(UserEntity.class);
        Frequency frequency = Frequency.WEEKLY;
        Category category = Category.HOBBY;

        List<AccountBook> mockBooks = List.of(
                new AccountBook(1L, "볼링", category, CategoryType.SPEND, 15000, null, "memo1", user, null, null, null),
                new AccountBook(2L, "영화", category, CategoryType.SPEND, 12000, null, "memo2", user, null, null, null)
        );

        given(accountBookRepository.findByCategory(anyLong(), any(), any(), any(LocalDateTime.class), any(LocalDateTime.class))).willReturn(mockBooks);

        // when
        List<AccountBookSpendResponse> result = statService.categoryList(user.getId(), frequency, category);

        // then
        assertThat(2).isEqualTo(result.size());
    }

    @Test
    @DisplayName("주기별 절약금액을 가져온다..")
    void consume() {
        // given
        UserEntity user = mock(UserEntity.class);
        Frequency frequency = Frequency.DAILY;
        Category category = Category.TRANSPORTATION;
        PeriodRange period = frequency.calcPeriod();

        given(accountBookRepository.sumSpending(anyLong(), eq(CategoryType.SPEND), eq(category), any(), any()))
                .willReturn(10000, 15000);

        // when
        AccountBookConsumeResponse result = statService.consume(user.getId(), frequency, category);

        // then
        assertThat(5000).isEqualTo(result.consume());
    }

    @Test
    @DisplayName("해당 주기에 값이 없다면 예외를 반환한다.")
    void getStatListFail() {
        // given
        UserEntity user = mock(UserEntity.class);
        Frequency frequency = Frequency.MONTHLY;
        LocalDateTime start = frequency.calcStartDate();
        given(accountBookRepository.findTop3Categories(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), eq(PageRequest.of(0, 3))))
                .willReturn(List.of());

        // when
        // then
        assertThatThrownBy(() -> statService.statList(user.getId(), frequency))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 소비내역입니다.");
    }

    @Test
    @DisplayName("해당 카테고리에 값이 없다면 예외를 반환한다.")
    void getCategoryListFail() {
        // given
        UserEntity user = mock(UserEntity.class);
        Frequency frequency = Frequency.MONTHLY;
        LocalDateTime start = frequency.calcStartDate();
        given(accountBookRepository.findByCategory(anyLong(), any(), any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of());

        // when
        // then
        assertThatThrownBy(() -> statService.categoryList(user.getId(), frequency, Category.HOBBY))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 소비내역입니다.");
    }
}