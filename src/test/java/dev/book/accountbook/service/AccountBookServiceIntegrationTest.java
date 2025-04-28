package dev.book.accountbook.service;

import dev.book.accountbook.dto.request.AccountBookIncomeRequest;
import dev.book.accountbook.dto.request.AccountBookListRequest;
import dev.book.accountbook.dto.request.AccountBookSpendRequest;
import dev.book.accountbook.dto.request.Repeat;
import dev.book.accountbook.dto.response.AccountBookIncomeListResponse;
import dev.book.accountbook.dto.response.AccountBookIncomeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendListResponse;
import dev.book.accountbook.dto.response.AccountBookSpendResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.exception.accountbook.AccountBookErrorException;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.repository.BudgetRepository;
import dev.book.accountbook.type.CategoryType;
import dev.book.accountbook.type.Frequency;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.global.config.security.jwt.JwtAuthenticationToken;
import dev.book.global.entity.Category;
import dev.book.global.repository.CategoryRepository;
import dev.book.user.entity.UserEntity;
import dev.book.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AccountBookServiceIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AccountBookService accountBookService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountBookRepository accountBookRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
    }

    static CustomUserDetails userDetails;
    static LocalDateTime endTime = LocalDateTime.of(2025, 5, 31, 0, 0);
    static LocalDate occurredAt = LocalDate.of(2025, 4, 22);
    static List<AccountBook> savedBooks = null;

    @BeforeEach
    public void createUser() {
        UserEntity user = UserEntity.builder()
                .email("test@example.com")
                .name("홍길동")
                .nickname("길동이")
                .profileImageUrl("test")
                .build();
        userRepository.save(user);

        userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(userDetails, userDetails.getAuthorities()));

        List<Category> categories = categoryRepository.findAll();

        List<AccountBook> books = List.of(
                new AccountBook("점심 식비", CategoryType.SPEND, 8000, null, "김밥천국", user, null, null, null, categories.get(0), LocalDate.of(2025, 4, 21)),
                new AccountBook("카페", CategoryType.SPEND, 4500, null, "이디야 아메리카노", user, null, null, null, categories.get(1), LocalDate.of(2025, 4, 22)),
                new AccountBook("마트 장보기", CategoryType.SPEND, 23000, null, "라면, 참치", user, null, null, null, categories.get(2), LocalDate.of(2025, 4, 23)),
                new AccountBook("술자리", CategoryType.SPEND, 50000, null, "친구들과 회식", user, null, null, null, categories.get(3), LocalDate.of(2025, 4, 23)),
                new AccountBook("옷 쇼핑", CategoryType.SPEND, 67000, null, "봄 자켓", user, null, null, null, categories.get(4), LocalDate.of(2025, 4, 24)),
                new AccountBook("헬스장 등록", CategoryType.SPEND, 60000, null, "한 달 등록", user, null, null, null, categories.get(6), LocalDate.of(2025, 4, 24)),
                new AccountBook("미용실", CategoryType.SPEND, 15000, null, "컷트", user, null, null, null, categories.get(9), LocalDate.of(2025, 4, 25)),
                new AccountBook("버스비", CategoryType.SPEND, 1400, null, "출근길", user, null, null, null, categories.get(10), LocalDate.of(2025, 4, 21)),
                new AccountBook("이체", CategoryType.INCOME, 300000, null, "돈 빌려준거 받음", user, null, null, 25, categories.get(17), LocalDate.of(2025, 4, 26)),
                new AccountBook("급여", CategoryType.INCOME, 3000000, null, "4월 급여", user, Frequency.MONTHLY, null, 25, categories.get(18), LocalDate.of(2025, 4, 25)),
                new AccountBook("저축", CategoryType.INCOME, 500000, null, "월 저축", user, Frequency.MONTHLY, null, 25, categories.get(19), LocalDate.of(2025, 4, 25))
        );

        savedBooks = accountBookRepository.saveAll(books);
    }

    @AfterEach
    public void cleanUp() {
        budgetRepository.deleteAllInBatch();
        accountBookRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("지출을 상세 조회한다.")
    @WithMockUser(username = "test@test.com")
    void getSpendOne() {
        // given
        UserEntity user = userDetails.user();
        AccountBookSpendRequest request = new AccountBookSpendRequest(
                "점심 식비", 8000, "김밥천국", null, LocalDate.of(2025, 4, 21), null, "food");

        // when
        AccountBookSpendResponse response = accountBookService.getSpendOne(savedBooks.get(0).getId(), user.getId());

        // then
        assertThat(response.title()).isEqualTo(request.title());
        assertThat(response.category()).isEqualTo("식비");
        assertThat(response.amount()).isEqualTo(request.amount());
        assertThat(response.memo()).isEqualTo(request.memo());
        assertThat(response.occurredAt()).isEqualTo(request.occurredAt());
        assertThat(response.repeat().frequency()).isNull();
        assertThat(response.repeat().month()).isNull();
        assertThat(response.repeat().day()).isNull();
    }

    @Test
    @DisplayName("지출 조회에 실패하면 예외가 발생한다.")
    @WithMockUser(username = "test@test.com")
    void getSpendOneFail() {
        // given
        UserEntity user = userDetails.user();

        // when
        // then
        assertThatThrownBy(() -> accountBookService.getSpendOne(1L, user.getId()))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 소비내역입니다.");
    }

    @Test
    @DisplayName("지출 리스트를 반환한다.")
    @WithMockUser(username = "test@test.com")
    void getSpendList() {
        // given
        UserEntity user = userDetails.user();

        // when
        AccountBookSpendListResponse responses = accountBookService.getSpendList(user.getId(), 1);

        // then
        List<AccountBookSpendResponse> responseList = responses.accountBookSpendResponseList();
        assertThat(responseList).hasSize(8);
        assertThat(responseList)
                .extracting(AccountBookSpendResponse::title)
                .containsExactly("미용실", "옷 쇼핑", "헬스장 등록", "마트 장보기", "술자리", "카페", "점심 식비", "버스비");
        ;
    }

    @Test
    @DisplayName("지출을 등록한다.")
    @WithMockUser(username = "test@test.com")
    void createSpend() {
        // given
        UserEntity user = userDetails.user();
        Repeat repeat = new Repeat(Frequency.MONTHLY, 3, 15);

        AccountBookSpendRequest request = new AccountBookSpendRequest(
                "핫도그", 3000, "야식", endTime, occurredAt, repeat, "food");

        // when
        AccountBookSpendResponse response = accountBookService.createSpend(request, user);

        // then
        assertThat(response.title()).isEqualTo(request.title());
        assertThat(response.category()).isEqualTo("식비");
        assertThat(response.amount()).isEqualTo(request.amount());
        assertThat(response.memo()).isEqualTo(request.memo());
        assertThat(response.endDate()).isEqualTo(request.endDate());
        assertThat(response.repeat().frequency()).isEqualTo(repeat.frequency());
        assertThat(response.repeat().month()).isEqualTo(repeat.month());
        assertThat(response.repeat().day()).isEqualTo(repeat.day());
    }

    @Test
    @DisplayName("지출을 수정한다.")
    @WithMockUser(username = "test@test.com")
    void modifySpend() {
        // given
        UserEntity user = userDetails.user();
        Repeat repeat = new Repeat(Frequency.MONTHLY, 3, 15);

        AccountBookSpendRequest modifiedRequest = new AccountBookSpendRequest(
                "샐러드", 7000, "건강식", LocalDateTime.of(2025, 6, 1, 12, 0), occurredAt, repeat, "food");

        // when
        AccountBookSpendResponse response = accountBookService.modifySpend(modifiedRequest, savedBooks.get(0).getId(), user.getId());

        // then
        assertThat(response.title()).isEqualTo(modifiedRequest.title());
        assertThat(response.category()).isEqualTo("식비");
        assertThat(response.amount()).isEqualTo(modifiedRequest.amount());
        assertThat(response.memo()).isEqualTo(modifiedRequest.memo());
        assertThat(response.endDate()).isEqualTo(modifiedRequest.endDate());
        assertThat(response.repeat().frequency()).isEqualTo(repeat.frequency());
        assertThat(response.repeat().month()).isEqualTo(repeat.month());
        assertThat(response.repeat().day()).isEqualTo(repeat.day());
    }

    @Test
    @DisplayName("지출을 수정을 위한 ID를 찾지 못하면 예외가 발생한다.")
    @WithMockUser(username = "test@test.com")
    void modifySpendFail() {
        // given
        UserEntity user = userDetails.user();
        Repeat repeat = new Repeat(Frequency.MONTHLY, 3, 15);
        AccountBookSpendRequest modifiedRequest = new AccountBookSpendRequest(
                "샐러드", 7000, "건강식", endTime, occurredAt, repeat, "food");

        // when
        // then
        assertThatThrownBy(() -> accountBookService.modifySpend(modifiedRequest, 1L, user.getId()))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 소비내역입니다.");
    }

    @Test
    @DisplayName("지출 내역을 삭제한다.")
    @WithMockUser(username = "test@test.com")
    void deleteSpend() {
        // given
        UserEntity user = userDetails.user();
        Repeat repeat = new Repeat(Frequency.MONTHLY, 3, 15);

        // when
        boolean status = accountBookService.deleteSpend(savedBooks.get(0).getId(), user.getId());

        // then
        assertThat(status).isTrue();
    }

    @Test
    @DisplayName("지출 삭제를 위한 ID를 찾지 못하면 예외가 발생한다.")
    @WithMockUser(username = "test@test.com")
    void deleteSpendFail() {
        // given
        UserEntity user = userDetails.user();

        // when
        // then
        assertThatThrownBy(() -> accountBookService.deleteSpend(1L, user.getId()))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 소비내역입니다.");
    }

    @Test
    @DisplayName("수입을 상세 조회한다.")
    @WithMockUser(username = "test@test.com")
    void getIncomeOne() {
        // given
        UserEntity user = userDetails.user();
        Repeat repeat = new Repeat(Frequency.MONTHLY, null, 25);
        AccountBookIncomeRequest request = new AccountBookIncomeRequest(
                "급여", 3000000, "4월 급여", null,  LocalDate.of(2025, 4, 25), repeat, "salary");

        // when
        AccountBookIncomeResponse response = accountBookService.getIncomeOne(savedBooks.get(9).getId(), user.getId());

        // then
        assertThat(response.title()).isEqualTo(request.title());
        assertThat(response.category()).isEqualTo("급여");
        assertThat(response.amount()).isEqualTo(request.amount());
        assertThat(response.memo()).isEqualTo(request.memo());
        assertThat(response.endDate()).isEqualTo(request.endDate());
        assertThat(response.occurredAt()).isEqualTo(request.occurredAt());
        assertThat(response.repeat().frequency()).isEqualTo(repeat.frequency());
        assertThat(response.repeat().month()).isEqualTo(repeat.month());
        assertThat(response.repeat().day()).isEqualTo(repeat.day());
    }


    @Test
    @DisplayName("수입 조회에 실패하면 예외가 발생한다.")
    @WithMockUser(username = "test@test.com")
    void getIncomeOneFail() {
        // given
        UserEntity user = userDetails.user();

        // when // then
        assertThatThrownBy(() -> accountBookService.getIncomeOne(1L, user.getId()))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 수입내역입니다.");
    }

    @Test
    @DisplayName("수입 리스트를 반환한다.")
    @WithMockUser(username = "test@test.com")
    void getIncomeList() {
        // given
        UserEntity user = userDetails.user();
        AccountBookListRequest listRequest = new AccountBookListRequest(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30));

        // when
        AccountBookIncomeListResponse responses = accountBookService.getIncomeList(user.getId(), 1);

        // then
        List<AccountBookIncomeResponse> responseList = responses.accountBookIncomeResponseList();
        assertThat(responseList).hasSize(3);
        assertThat(responseList)
                .extracting(AccountBookIncomeResponse::title)
                .containsExactly("이체", "급여", "저축");
    }

    @Test
    @DisplayName("수입을 등록한다.")
    @WithMockUser(username = "test@test.com")
    void createIncome() {
        // given
        UserEntity user = userDetails.user();
        Repeat repeat = new Repeat(Frequency.MONTHLY, 1, 25);
        AccountBookIncomeRequest request = new AccountBookIncomeRequest(
                "월급", 3000000, "회사 월급", endTime, occurredAt, repeat, "salary");

        // when
        AccountBookIncomeResponse response = accountBookService.createIncome(request, user);

        // then
        assertThat(response.title()).isEqualTo(request.title());
        assertThat(response.category()).isEqualTo("급여");
        assertThat(response.amount()).isEqualTo(request.amount());
        assertThat(response.memo()).isEqualTo(request.memo());
        assertThat(response.endDate()).isEqualTo(request.endDate());
        assertThat(response.repeat().frequency()).isEqualTo(repeat.frequency());
        assertThat(response.repeat().month()).isEqualTo(repeat.month());
        assertThat(response.repeat().day()).isEqualTo(repeat.day());
    }

    @Test
    @DisplayName("수입을 수정한다.")
    @WithMockUser(username = "test@test.com")
    void modifyIncome() {
        // given
        UserEntity user = userDetails.user();
        Repeat repeat = new Repeat(Frequency.MONTHLY, 1, 25);
        AccountBookIncomeRequest modifiedRequest = new AccountBookIncomeRequest(
                "보너스", 2000000, "성과급", endTime, occurredAt, repeat, "salary");

        // when
        AccountBookIncomeResponse response = accountBookService.modifyIncome(savedBooks.get(8).getId(), modifiedRequest, user.getId());

        // then
        assertThat(response.title()).isEqualTo(modifiedRequest.title());
        assertThat(response.category()).isEqualTo("급여");
        assertThat(response.amount()).isEqualTo(modifiedRequest.amount());
        assertThat(response.memo()).isEqualTo(modifiedRequest.memo());
        assertThat(response.endDate()).isEqualTo(modifiedRequest.endDate());
        assertThat(response.repeat().frequency()).isEqualTo(repeat.frequency());
        assertThat(response.repeat().month()).isEqualTo(repeat.month());
        assertThat(response.repeat().day()).isEqualTo(repeat.day());
    }

    @Test
    @DisplayName("수입 수정을 위한 ID를 찾지 못하면 예외가 발생한다.")
    @WithMockUser(username = "test@test.com")
    void modifyIncomeFail() {
        // given
        UserEntity user = userDetails.user();
        Repeat repeat = new Repeat(Frequency.MONTHLY, 1, 25);
        AccountBookIncomeRequest modifiedRequest = new AccountBookIncomeRequest(
                "보너스", 2000000, "성과급", endTime, occurredAt, repeat, "salary");

        // when // then
        assertThatThrownBy(() -> accountBookService.modifyIncome(1L, modifiedRequest, user.getId()))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 수입내역입니다.");
    }

    @Test
    @DisplayName("수입 내역을 삭제한다.")
    @WithMockUser(username = "test@test.com")
    void deleteIncome() {
        // given
        UserEntity user = userDetails.user();

        // when
        boolean status = accountBookService.deleteIncome(savedBooks.get(8).getId(), user.getId());

        // then
        assertThat(status).isTrue();
    }

    @Test
    @DisplayName("수입 삭제를 위한 ID를 찾지 못하면 예외가 발생한다.")
    @WithMockUser(username = "test@test.com")
    void deleteIncomeFail() {
        // given
        UserEntity user = userDetails.user();

        // when
        // then
        assertThatThrownBy(() -> accountBookService.deleteIncome(0L, user.getId()))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 수입내역입니다.");
    }

    @Test
    @DisplayName("카테고리별 지출 리스트를 조회한다.")
    @WithMockUser(username = "test@test.com")
    void getCategorySpendList() {
        // given
        UserEntity user = userDetails.user();

        // when
        AccountBookSpendListResponse foodResponses = accountBookService.getCategorySpendList("food", user.getId(), 1);

        // then
        List<AccountBookSpendResponse> responseList = foodResponses.accountBookSpendResponseList();
        assertThat(responseList).hasSize(1);
        assertThat(responseList.get(0).title()).isEqualTo("점심 식비");
        assertThat(responseList.get(0).category()).isEqualTo("식비");
    }
}
