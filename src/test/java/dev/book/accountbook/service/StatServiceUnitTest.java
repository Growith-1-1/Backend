package dev.book.accountbook.service;

import dev.book.accountbook.dto.response.AccountBookConsumeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendListResponse;
import dev.book.accountbook.dto.response.AccountBookStatResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.type.CategoryType;
import dev.book.accountbook.type.Frequency;
import dev.book.global.entity.Category;
import dev.book.user.entity.UserEntity;
import dev.book.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class StatServiceUnitTest {
    @Mock
    ApplicationEventPublisher publisher;
    @Mock
    private AccountBookRepository accountBookRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private StatService statService;

    static LocalDate occurredAt = LocalDate.of(2025, 4, 22);

    @Test
    @DisplayName("주기를 기준으로 소비내역을 가져온다.")
    void statList() {
        // given
        UserEntity user = mock(UserEntity.class);
        Frequency frequency = Frequency.MONTHLY;
        LocalDate start = frequency.calcStartDate();
        List<AccountBookStatResponse> mockResponses = List.of(
                new AccountBookStatResponse("food", 10000L),
                new AccountBookStatResponse("cafe_snack", 8000L),
                new AccountBookStatResponse("shopping", 5000L)
        );

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(accountBookRepository.findTopCategoriesByUserAndPeriod(anyLong(), eq(start), any(LocalDate.class), any()))
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
        Category category = new Category("hobby", "취미");

        Page<AccountBook> mockBooks = new PageImpl<>(List.of(
                new AccountBook("볼링", CategoryType.SPEND, 15000, null, "memo1", user, null, null, null, category, occurredAt),
                new AccountBook("영화", CategoryType.SPEND, 12000, null, "memo2", user, null, null, null, category, occurredAt)
        ));

        Pageable pageable = mock(Pageable.class);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(accountBookRepository.findByCategory(anyLong(), any(), any(), any(LocalDate.class), any(LocalDate.class), pageable)).willReturn(mockBooks);

        // when
        AccountBookSpendListResponse result = statService.categoryList(user.getId(), frequency, "hobby", 1);

        // then
        assertThat(2).isEqualTo(result.accountBookSpendResponseList().size());
    }

    @Test
    @DisplayName("주기별 절약금액을 가져온다.")
    void consume() {
        // given
        UserEntity user = mock(UserEntity.class);
        Frequency frequency = Frequency.DAILY;

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(accountBookRepository.sumSpending(anyLong(), eq(CategoryType.SPEND), any(), any()))
                .willReturn(10000, 15000);

        // when
        AccountBookConsumeResponse result = statService.consume(user.getId(), frequency);

        // then
        assertThat(result.consume()).isEqualTo(5000);
    }
}