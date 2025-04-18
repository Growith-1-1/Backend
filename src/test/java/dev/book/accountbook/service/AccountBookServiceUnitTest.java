package dev.book.accountbook.service;

import dev.book.accountbook.dto.event.SpendCreatedEvent;
import dev.book.accountbook.dto.request.AccountBookIncomeRequest;
import dev.book.accountbook.dto.request.AccountBookSpendRequest;
import dev.book.accountbook.dto.request.Repeat;
import dev.book.accountbook.dto.response.AccountBookIncomeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.exception.accountbook.AccountBookErrorException;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.type.Category;
import dev.book.accountbook.type.CategoryType;
import dev.book.accountbook.type.Frequency;
import dev.book.user.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AccountBookServiceUnitTest {
    @Mock
    private AccountBookRepository accountBookRepository;
    @Mock
    ApplicationEventPublisher publisher;
    @InjectMocks
    private AccountBookService accountBookService;

    private final Long userId = 1L;
    private final Long accountBookId = 100L;

    @Test
    @DisplayName("상세 소비 내역을 불러 올 수 있다.")
    void getSpendOne() {
        // given
        AccountBook accountBook = mock(AccountBook.class);
        given(accountBookRepository.findByIdAndUserId(accountBookId, userId)).willReturn(Optional.of(accountBook));

        // when
        AccountBookSpendResponse result = accountBookService.getSpendOne(accountBookId, userId);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("상세 소비 내역을 찾을 수 없으면 예외가 발생한다.")
    void failGetSpendOne() {
        // given
        given(accountBookRepository.findByIdAndUserId(accountBookId, userId)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> accountBookService.getSpendOne(accountBookId, userId))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 소비내역입니다.");
    }

    @Test
    @DisplayName("소비 내역 리스트를 불러온다.")
    void getSpendList() {
        // given
        List<AccountBook> mockList = List.of(mock(AccountBook.class));
        given(accountBookRepository.findAllByUserIdAndTypeOrderByUpdatedAtDesc(userId, CategoryType.SPEND)).willReturn(mockList);

        // when
        List<AccountBookSpendResponse> result = accountBookService.getSpendList(userId);

        // then
        assertThat(result.size()).isOne();
    }

    @Test
    @DisplayName("단건 소비 내역을 저장한다.")
    void createSpend() {
        // given
        UserEntity user = mock(UserEntity.class);
        AccountBookSpendRequest request = new AccountBookSpendRequest("커피", Category.CAFE_SNACK, 1000, "스타벅스", null, null);
        AccountBook saved = request.toEntity(user);

        given(accountBookRepository.save(any(AccountBook.class))).willReturn(saved);
        willDoNothing().given(publisher).publishEvent(any(SpendCreatedEvent.class));

        // when
        AccountBookSpendResponse result = accountBookService.createSpend(request, user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.category()).isEqualTo(request.category());
        assertThat(result.amount()).isEqualTo(request.amount());
        assertThat(result.memo()).isEqualTo(request.memo());
        assertThat(result.endDate()).isEqualTo(request.endDate());
    }

    @Test
    @DisplayName("정기 소비 내역을 저장한다.")
    void createRegularSpend() {
        // given
        UserEntity user = mock(UserEntity.class);
        AccountBookSpendRequest request = new AccountBookSpendRequest("커피", Category.CAFE_SNACK, 1000, "스타벅스", null, new Repeat(Frequency.MONTHLY, null, 15));
        AccountBook saved = request.toEntity(user);

        given(accountBookRepository.save(any(AccountBook.class))).willReturn(saved);

        // when
        AccountBookSpendResponse result = accountBookService.createSpend(request, user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.category()).isEqualTo(request.category());
        assertThat(result.amount()).isEqualTo(request.amount());
        assertThat(result.memo()).isEqualTo(request.memo());
        assertThat(result.endDate()).isEqualTo(request.endDate());
        assertThat(result.repeat().frequency()).isEqualTo(request.repeat().frequency());
        assertThat(result.repeat().month()).isNull();
        assertThat(result.repeat().day()).isEqualTo(request.repeat().day());
    }

    @Test
    @DisplayName("소비 내역을 수정한다.")
    void modifySpend() {
        Long id = 1L;
        Long userId = 10L;
        AccountBook accountBook = new AccountBook(1L, "before modify", Category.FOOD, CategoryType.SPEND, 1000, null, "before memo", mock(UserEntity.class), null, null, null);

        Repeat repeat = new Repeat(Frequency.MONTHLY, null, 15);
        AccountBookSpendRequest request = new AccountBookSpendRequest(
                "커피", Category.CAFE_SNACK, 5000, "스타벅스", LocalDateTime.of(2025, 4, 1, 0, 0), repeat);

        given(accountBookRepository.findByIdAndUserId(id, userId)).willReturn(Optional.of(accountBook));

        // when
        AccountBookSpendResponse result = accountBookService.modifySpend(request, id, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("커피");
        assertThat(result.category()).isEqualTo(Category.CAFE_SNACK);
        assertThat(result.amount()).isEqualTo(5000);
        assertThat(result.memo()).isEqualTo("스타벅스");
        assertThat(result.endDate()).isEqualTo(LocalDateTime.of(2025, 4, 1, 0, 0));
    }

    @Test
    @DisplayName("소비 내역을 삭제할 수 있다.")
    void deleteSpend() {
        // given
        AccountBook accountBook = mock(AccountBook.class);
        given(accountBookRepository.findByIdAndUserId(accountBookId, userId)).willReturn(Optional.of(accountBook));

        // when
        boolean result = accountBookService.deleteSpend(accountBookId, userId);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("소비 내역이 없으면 예외가 발생한다.")
    void failDeleteSpend() {
        // given
        given(accountBookRepository.findByIdAndUserId(accountBookId, userId)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> accountBookService.deleteSpend(accountBookId, userId))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 소비내역입니다.");
    }

    @Test
    @DisplayName("상세 수입 내역을 불러 올 수 있다.")
    void getIncomeOne() {
        // given
        AccountBook accountBook = mock(AccountBook.class);
        given(accountBookRepository.findByIdAndUserId(userId, accountBookId)).willReturn(Optional.of(accountBook));

        // when
        AccountBookIncomeResponse result = accountBookService.getIncomeOne(userId, accountBookId);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("상세 수입 내역을 찾을 수 없으면 예외가 발생한다.")
    void failGetIncomeOne() {
        // given
        given(accountBookRepository.findByIdAndUserId(accountBookId, userId)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> accountBookService.getIncomeOne(accountBookId, userId))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 수입내역입니다.");
    }

    @Test
    @DisplayName("수입 내역 리스트를 불러온다.")
    void getIncomeList() {
        // given
        List<AccountBook> mockList = List.of(mock(AccountBook.class));
        given(accountBookRepository.findAllByUserIdAndTypeOrderByUpdatedAtDesc(userId, CategoryType.INCOME)).willReturn(mockList);

        // when
        List<AccountBookIncomeResponse> result = accountBookService.getIncomeList(userId);

        // then
        assertThat(result.size()).isOne();
    }

    @Test
    @DisplayName("단건 수입 내역을 저장한다.")
    void createIncome() {
        // given
        UserEntity user = mock(UserEntity.class);
        AccountBookIncomeRequest request = new AccountBookIncomeRequest("커피", Category.CAFE_SNACK, 1000, "스타벅스", null, null);
        AccountBook saved = request.toEntity(user);

        given(accountBookRepository.save(any(AccountBook.class))).willReturn(saved);

        // when
        AccountBookIncomeResponse result = accountBookService.createIncome(request, user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.category()).isEqualTo(request.category());
        assertThat(result.amount()).isEqualTo(request.amount());
        assertThat(result.memo()).isEqualTo(request.memo());
        assertThat(result.endDate()).isEqualTo(request.endDate());
    }

    @Test
    @DisplayName("정기 수입 내역을 저장한다.")
    void createRegularIncome() {
        // given
        UserEntity user = mock(UserEntity.class);
        AccountBookIncomeRequest request = new AccountBookIncomeRequest("커피", Category.CAFE_SNACK, 1000, "스타벅스", null, new Repeat(Frequency.MONTHLY, null, 15));
        AccountBook saved = request.toEntity(user);

        given(accountBookRepository.save(any(AccountBook.class))).willReturn(saved);

        // when
        AccountBookIncomeResponse result = accountBookService.createIncome(request, user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.category()).isEqualTo(request.category());
        assertThat(result.amount()).isEqualTo(request.amount());
        assertThat(result.memo()).isEqualTo(request.memo());
        assertThat(result.endDate()).isEqualTo(request.endDate());
        assertThat(result.repeat().frequency()).isEqualTo(request.repeat().frequency());
        assertThat(result.repeat().month()).isNull();
        assertThat(result.repeat().day()).isEqualTo(request.repeat().day());
    }

    @Test
    @DisplayName("수입 내역을 수정한다.")
    void modifyIncome() {
        Long id = 1L;
        Long userId = 10L;
        AccountBook accountBook = new AccountBook(1L, "before modify", Category.FOOD, CategoryType.INCOME, 1000, null, "before memo", mock(UserEntity.class), null, null, null);

        Repeat repeat = new Repeat(Frequency.MONTHLY, null, 15);
        AccountBookIncomeRequest request = new AccountBookIncomeRequest(
                "커피", Category.CAFE_SNACK, 5000, "스타벅스", LocalDateTime.of(2025, 4, 1, 0, 0), repeat);

        given(accountBookRepository.findByIdAndUserId(id, userId)).willReturn(Optional.of(accountBook));

        // when
        AccountBookIncomeResponse result = accountBookService.modifyIncome(id, request, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("커피");
        assertThat(result.category()).isEqualTo(Category.CAFE_SNACK);
        assertThat(result.amount()).isEqualTo(5000);
        assertThat(result.memo()).isEqualTo("스타벅스");
        assertThat(result.endDate()).isEqualTo(LocalDateTime.of(2025, 4, 1, 0, 0));
    }

    @Test
    @DisplayName("수입 내역을 삭제할 수 있다.")
    void deleteIncome() {
        // given
        AccountBook accountBook = mock(AccountBook.class);
        given(accountBookRepository.findByIdAndUserId(accountBookId, userId)).willReturn(Optional.of(accountBook));

        // when
        boolean result = accountBookService.deleteSpend(accountBookId, userId);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("소비 내역이 없으면 예외가 발생한다.")
    void failDeleteIncome() {
        // given
        given(accountBookRepository.findByIdAndUserId(accountBookId, userId)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> accountBookService.deleteIncome(accountBookId, userId))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 수입내역입니다.");
    }
}