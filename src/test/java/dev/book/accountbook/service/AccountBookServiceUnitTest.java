package dev.book.accountbook.service;

import dev.book.accountbook.dto.request.AccountBookIncomeRequest;
import dev.book.accountbook.dto.request.AccountBookListRequest;
import dev.book.accountbook.dto.request.AccountBookSpendRequest;
import dev.book.accountbook.dto.request.Repeat;
import dev.book.accountbook.dto.response.AccountBookIncomeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.exception.accountbook.AccountBookErrorException;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.repository.BudgetRepository;
import dev.book.accountbook.type.CategoryType;
import dev.book.accountbook.type.Frequency;
import dev.book.achievement.achievement_user.IndividualAchievementStatusService;
import dev.book.challenge.rank.SpendCreatedRankingEvent;
import dev.book.global.entity.Category;
import dev.book.global.repository.CategoryRepository;
import dev.book.user.entity.UserEntity;
import dev.book.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AccountBookServiceUnitTest {
    @Mock
    private AccountBookRepository accountBookRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    IndividualAchievementStatusService individualAchievementStatusService;
    @InjectMocks
    private AccountBookService accountBookService;

    private final Long userId = 1L;
    private final Long accountBookId = 100L;

    static LocalDateTime endTime = LocalDateTime.of(2025, 5, 31, 0, 0);
    static LocalDate occurredAt = LocalDate.of(2025, 4, 22);
    static Repeat repeat = new Repeat(Frequency.MONTHLY, null, 15);

    @Test
    @DisplayName("상세 소비 내역을 불러 올 수 있다.")
    void getSpendOne() {
        // given
        AccountBookSpendRequest request = new AccountBookSpendRequest(
                "핫도그", 3000, "야식", endTime, occurredAt, repeat, "food");
        UserEntity userEntity = mock(UserEntity.class);
        Category category = new Category("food", "식비");
        AccountBook accountBook = request.toEntity(userEntity, category);
        given(accountBookRepository.findByIdAndUserId(accountBookId, userId)).willReturn(Optional.of(accountBook));
        given(userRepository.existsById(userId)).willReturn(true);

        // when
        AccountBookSpendResponse result = accountBookService.getSpendOne(accountBookId, userId);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("상세 소비 내역을 찾을 수 없으면 예외가 발생한다.")
    void failGetSpendOne() {
        // given
        UserEntity userEntity = mock(UserEntity.class);
        given(userRepository.existsById(userId)).willReturn(true);
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
        AccountBookSpendRequest request = new AccountBookSpendRequest(
                "핫도그", 3000, "야식", endTime, occurredAt, repeat, "food");
        UserEntity userEntity = mock(UserEntity.class);
        Category category = new Category("food", "식비");
        AccountBook accountBook = request.toEntity(userEntity, category);

        List<AccountBook> mockList = List.of(accountBook, accountBook);
        given(accountBookRepository.findAllByTypeAndPeriod(anyLong(), any(), any(), any())).willReturn(mockList);

        // when
        List<AccountBookSpendResponse> result = accountBookService.getSpendList(userId, new AccountBookListRequest(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30)));

        // then
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("단건 소비 내역을 저장한다.")
    void createSpend() {
        // given
        UserEntity user = mock(UserEntity.class);
        AccountBookSpendRequest request = new AccountBookSpendRequest(
                "핫도그", 3000, "야식", endTime, occurredAt, repeat, "food");
        AccountBook saved = request.toEntity(user, new Category("food", "식비"));

        given(accountBookRepository.save(any(AccountBook.class))).willReturn(saved);
        given(categoryRepository.findByCategory("food")).willReturn(Optional.of(new Category("food", "식비")));
        willDoNothing().given(publisher).publishEvent(any(SpendCreatedRankingEvent.class));

        // when
        AccountBookSpendResponse result = accountBookService.createSpend(request, user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.category()).isEqualTo("식비");
        assertThat(result.amount()).isEqualTo(request.amount());
        assertThat(result.memo()).isEqualTo(request.memo());
        assertThat(result.endDate()).isEqualTo(request.endDate());
    }

    @Test
    @DisplayName("정기 소비 내역을 저장한다.")
    void createRegularSpend() {
        // given
        UserEntity user = mock(UserEntity.class);
        AccountBookSpendRequest request = new AccountBookSpendRequest(
                "핫도그", 3000, "야식", endTime, occurredAt, repeat, "food");
        AccountBook saved = request.toEntity(user, new Category("food", "식비"));

        given(accountBookRepository.save(any(AccountBook.class))).willReturn(saved);
        given(categoryRepository.findByCategory("food")).willReturn(Optional.of(new Category("food", "식비")));

        // when
        AccountBookSpendResponse result = accountBookService.createSpend(request, user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.category()).isEqualTo("식비");
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
        AccountBook accountBook = new AccountBook("before modify", CategoryType.SPEND, 1000, null, "before memo", mock(UserEntity.class), null, null, null, new Category("food", "식비"), occurredAt);

        Repeat repeat = new Repeat(Frequency.MONTHLY, null, 15);
        AccountBookSpendRequest request = new AccountBookSpendRequest(
                "커피", 5000, "스타벅스", LocalDateTime.of(2025, 4, 1, 0, 0), occurredAt, repeat, "cafe_snack");

        given(accountBookRepository.findByIdAndUserId(id, userId)).willReturn(Optional.of(accountBook));
        given(categoryRepository.findByCategory("cafe_snack")).willReturn(Optional.of(new Category("cafe_snack", "카페 / 간식")));

        // when
        AccountBookSpendResponse result = accountBookService.modifySpend(request, id, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("커피");
        assertThat(result.category()).isEqualTo("카페 / 간식");
        assertThat(result.amount()).isEqualTo(5000);
        assertThat(result.memo()).isEqualTo("스타벅스");
        assertThat(result.endDate()).isEqualTo(LocalDateTime.of(2025, 4, 1, 0, 0));
        assertThat(result.occurredAt()).isEqualTo(occurredAt);
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
        AccountBookIncomeRequest request = new AccountBookIncomeRequest(
                "급여", 3000000, "곧 없어질 예정", endTime, occurredAt, repeat, "salary");
        UserEntity userEntity = mock(UserEntity.class);
        Category category = new Category("food", "식비");
        AccountBook accountBook = request.toEntity(userEntity, category);
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
        AccountBookIncomeRequest request = new AccountBookIncomeRequest(
                "급여", 3000000, "곧 없어질 예정", endTime, occurredAt, repeat, "salary");
        UserEntity userEntity = mock(UserEntity.class);
        Category category = new Category("food", "식비");
        AccountBook accountBook = request.toEntity(userEntity, category);
        List<AccountBook> mockList = List.of(accountBook, accountBook);
        given(accountBookRepository.findAllByTypeAndPeriod(anyLong(), any(), any(), any())).willReturn(mockList);

        // when
        List<AccountBookIncomeResponse> result = accountBookService.getIncomeList(userId, new AccountBookListRequest(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30)));

        // then
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("단건 수입 내역을 저장한다.")
    void createIncome() {
        // given
        UserEntity user = mock(UserEntity.class);
        AccountBookIncomeRequest request = new AccountBookIncomeRequest("급여", 3000000, "곧 없어질 예정", null, occurredAt, null, "salary");
        AccountBook saved = request.toEntity(user, new Category("salary", "급여"));

        given(accountBookRepository.save(any(AccountBook.class))).willReturn(saved);
        given(categoryRepository.findByCategory("salary")).willReturn(Optional.of(new Category("salary", "급여")));

        // when
        AccountBookIncomeResponse result = accountBookService.createIncome(request, user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.category()).isEqualTo("급여");
        assertThat(result.amount()).isEqualTo(request.amount());
        assertThat(result.memo()).isEqualTo(request.memo());
        assertThat(result.endDate()).isEqualTo(request.endDate());
    }

    @Test
    @DisplayName("정기 수입 내역을 저장한다.")
    void createRegularIncome() {
        // given
        UserEntity user = mock(UserEntity.class);
        AccountBookIncomeRequest request = new AccountBookIncomeRequest("급여", 3000000, "곧 없어질 예정", LocalDateTime.of(2025, 12, 31, 10, 0, 0), occurredAt, new Repeat(Frequency.MONTHLY, null, 10), "salary");
        AccountBook saved = request.toEntity(user, new Category("salary", "급여"));

        given(accountBookRepository.save(any(AccountBook.class))).willReturn(saved);
        given(categoryRepository.findByCategory("salary")).willReturn(Optional.of(new Category("salary", "급여")));

        // when
        AccountBookIncomeResponse result = accountBookService.createIncome(request, user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.category()).isEqualTo("급여");
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
        AccountBook accountBook = new AccountBook("before modify", CategoryType.SPEND, 1000, null, "before memo", mock(UserEntity.class), null, null, null, new Category("salary", "급여"), occurredAt);

        Repeat repeat = new Repeat(Frequency.MONTHLY, null, 15);
        AccountBookIncomeRequest request = new AccountBookIncomeRequest("급여", 3000000, "곧 없어질 예정", LocalDateTime.of(2025, 12, 31, 23, 59, 59), occurredAt, repeat, "salary");


        given(accountBookRepository.findByIdAndUserId(id, userId)).willReturn(Optional.of(accountBook));
        given(categoryRepository.findByCategory("salary")).willReturn(Optional.of(new Category("salary", "급여")));

        // when
        AccountBookIncomeResponse result = accountBookService.modifyIncome(id, request, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("급여");
        assertThat(result.category()).isEqualTo("급여");
        assertThat(result.amount()).isEqualTo(3000000);
        assertThat(result.memo()).isEqualTo("곧 없어질 예정");
        assertThat(result.endDate()).isEqualTo(LocalDateTime.of(2025, 12, 31, 23, 59, 59));
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