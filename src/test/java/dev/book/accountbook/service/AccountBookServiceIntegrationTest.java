package dev.book.accountbook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.book.accountbook.dto.request.AccountBookIncomeRequest;
import dev.book.accountbook.dto.request.AccountBookSpendRequest;
import dev.book.accountbook.dto.request.Repeat;
import dev.book.accountbook.dto.response.AccountBookIncomeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.exception.accountbook.AccountBookErrorException;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.type.Category;
import dev.book.accountbook.type.Frequency;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.global.config.security.jwt.JwtAuthenticationToken;
import dev.book.global.config.security.jwt.JwtUtil;
import dev.book.user.entity.UserEntity;
import dev.book.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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
    private AccountBookRepository accountBookRepository;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
    }

    static CustomUserDetails userDetails;

    @BeforeEach
    public void createUser() {
        UserEntity user = userRepository.save(UserEntity.builder()
                .email("test@test.com")
                .name("test")
                .nickname("nickname")
                .profileImageUrl("profile")
                .userCategory(List.of(Category.HOBBY))
                .build());
        userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(userDetails, userDetails.getAuthorities()));
    }

    @AfterEach
    public void cleanUp() {
        userRepository.deleteAllInBatch();
        accountBookRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("지출을 상세 조회한다.")
    @WithMockUser(username = "test@test.com")
    void getSpendOne() {
        // given
        UserEntity user = userDetails.user();
        Repeat repeat = new Repeat(Frequency.MONTHLY, 3, 15);
        AccountBookSpendRequest request = new AccountBookSpendRequest(
                "핫도그", Category.FOOD, 3000, "야식", LocalDateTime.of(2025, 5, 31, 0, 0), repeat);

        AccountBook accountBook = accountBookRepository.save(request.toEntity(user));

        // when
        AccountBookSpendResponse response = accountBookService.getSpendOne(accountBook.getId(), user.getId());

        // then
        assertThat(response.title()).isEqualTo(accountBook.getTitle());
        assertThat(response.category()).isEqualTo(accountBook.getCategory());
        assertThat(response.amount()).isEqualTo(accountBook.getAmount());
        assertThat(response.memo()).isEqualTo(accountBook.getMemo());
        assertThat(response.endDate()).isEqualTo(accountBook.getEndDate());
        assertThat(response.repeat().frequency()).isEqualTo(repeat.frequency());
        assertThat(response.repeat().month()).isEqualTo(repeat.month());
        assertThat(response.repeat().day()).isEqualTo(repeat.day());
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
        Repeat repeat = new Repeat(Frequency.MONTHLY, 3, 15);
        AccountBookSpendRequest request1 = new AccountBookSpendRequest(
                "핫도그", Category.FOOD, 3000, "야식", LocalDateTime.of(2025, 5, 31, 0, 0), repeat);
        AccountBookSpendRequest request2 = new AccountBookSpendRequest(
                "갤럭시S25 울트라", Category.SHOPPING, 1300000, "이건 못참지 ㅋ", LocalDateTime.of(2025, 5, 31, 0, 0), repeat);
        accountBookRepository.save(request1.toEntity(user));
        accountBookRepository.save(request2.toEntity(user));

        // when
        List<AccountBookSpendResponse> responses = accountBookService.getSpendList(user.getId());

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(AccountBookSpendResponse::title)
                .containsExactlyInAnyOrder("핫도그", "갤럭시S25 울트라");
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
                "핫도그", Category.FOOD, 3000, "야식", LocalDateTime.of(2025, 5, 31, 0, 0), repeat);

        // when
        AccountBookSpendResponse response = accountBookService.createSpend(request, user);

        // then
        assertThat(response.title()).isEqualTo(request.title());
        assertThat(response.category()).isEqualTo(request.category());
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

        AccountBookSpendRequest originalRequest = new AccountBookSpendRequest(
                "핫도그", Category.FOOD, 3000, "야식", LocalDateTime.of(2025, 5, 31, 0, 0), repeat);
        AccountBookSpendResponse created = accountBookService.createSpend(originalRequest, user);

        AccountBookSpendRequest modifiedRequest = new AccountBookSpendRequest(
                "샐러드", Category.FOOD, 7000, "건강식", LocalDateTime.of(2025, 6, 1, 12, 0), repeat);

        // when
        AccountBookSpendResponse response = accountBookService.modifySpend(modifiedRequest, created.id(), user.getId());

        // then
        assertThat(response.title()).isEqualTo(modifiedRequest.title());
        assertThat(response.category()).isEqualTo(modifiedRequest.category());
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
                "샐러드", Category.FOOD, 7000, "건강식", LocalDateTime.of(2025, 6, 1, 12, 0), repeat);

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
        AccountBookSpendRequest request = new AccountBookSpendRequest(
                "핫도그", Category.FOOD, 3000, "야식", LocalDateTime.of(2025, 5, 31, 0, 0), repeat);
        AccountBook accountBook = accountBookRepository.save(request.toEntity(user));

        // when
        boolean status = accountBookService.deleteSpend(accountBook.getId(), user.getId());

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
    @DisplayName("단건 수입 내역을 저장하고 조회한다.")
    @WithMockUser(username = "test@test.com")
    void getIncomOne() {
        // given
        UserEntity user = userDetails.user();
        Repeat repeat = new Repeat(Frequency.MONTHLY, 3, 15);
        AccountBookIncomeRequest request = new AccountBookIncomeRequest(
                "월급", Category.SALARY, 3000000, "회사 월급", LocalDateTime.of(2025, 5, 31, 0, 0), repeat);
        AccountBook accountBook = accountBookRepository.save(request.toEntity(user));

        // when
        AccountBookIncomeResponse response = accountBookService.getIncomeOne(accountBook.getId(), user.getId());

        // then
        assertThat(response.title()).isEqualTo(accountBook.getTitle());
        assertThat(response.category()).isEqualTo(accountBook.getCategory());
        assertThat(response.amount()).isEqualTo(accountBook.getAmount());
        assertThat(response.memo()).isEqualTo(accountBook.getMemo());
        assertThat(response.endDate()).isEqualTo(accountBook.getEndDate());
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
        Repeat repeat = new Repeat(Frequency.MONTHLY, 1, 25);
        AccountBookIncomeRequest request1 = new AccountBookIncomeRequest(
                "월급", Category.FINANCE, 3000000, "회사 월급", LocalDateTime.of(2025, 5, 25, 0, 0), repeat);
        AccountBookIncomeRequest request2 = new AccountBookIncomeRequest(
                "부수입", Category.FINANCE, 500000, "프리랜서", LocalDateTime.of(2025, 5, 28, 0, 0), repeat);

        accountBookRepository.save(request1.toEntity(user));
        accountBookRepository.save(request2.toEntity(user));

        // when
        List<AccountBookIncomeResponse> responses = accountBookService.getIncomeList(user.getId());

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(AccountBookIncomeResponse::title)
                .containsExactlyInAnyOrder("월급", "부수입");
    }

    @Test
    @DisplayName("수입을 등록한다.")
    @WithMockUser(username = "test@test.com")
    void createIncome() {
        // given
        UserEntity user = userDetails.user();
        Repeat repeat = new Repeat(Frequency.MONTHLY, 1, 25);
        AccountBookIncomeRequest request = new AccountBookIncomeRequest(
                "월급", Category.FINANCE, 3000000, "회사 월급", LocalDateTime.of(2025, 5, 25, 0, 0), repeat);

        // when
        AccountBookIncomeResponse response = accountBookService.createIncome(request, user);

        // then
        assertThat(response.title()).isEqualTo(request.title());
        assertThat(response.category()).isEqualTo(request.category());
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
        AccountBookIncomeRequest originalRequest = new AccountBookIncomeRequest(
                "월급", Category.FINANCE, 3000000, "회사 월급", LocalDateTime.of(2025, 5, 25, 0, 0), repeat);
        AccountBookIncomeResponse created = accountBookService.createIncome(originalRequest, user);

        AccountBookIncomeRequest modifiedRequest = new AccountBookIncomeRequest(
                "보너스", Category.FINANCE, 2000000, "성과급", LocalDateTime.of(2025, 6, 10, 0, 0), repeat);

        // when
        AccountBookIncomeResponse response = accountBookService.modifyIncome(created.id(), modifiedRequest, user.getId());

        // then
        assertThat(response.title()).isEqualTo(modifiedRequest.title());
        assertThat(response.category()).isEqualTo(modifiedRequest.category());
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
                "보너스", Category.FINANCE, 2000000, "성과급", LocalDateTime.of(2025, 6, 10, 0, 0), repeat);

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
        Repeat repeat = new Repeat(Frequency.MONTHLY, 1, 25);
        AccountBookIncomeRequest request = new AccountBookIncomeRequest(
                "월급", Category.FINANCE, 3000000, "회사 월급", LocalDateTime.of(2025, 5, 25, 0, 0), repeat);
        AccountBook accountBook = accountBookRepository.save(request.toEntity(user));

        // when
        boolean status = accountBookService.deleteIncome(accountBook.getId(), user.getId());

        // then
        assertThat(status).isTrue();
    }

    @Test
    @DisplayName("수입 삭제를 위한 ID를 찾지 못하면 예외가 발생한다.")
    @WithMockUser(username = "test@test.com")
    void deleteIncomeFail() {
        // given
        UserEntity user = userDetails.user();

        // when // then
        assertThatThrownBy(() -> accountBookService.deleteIncome(1L, user.getId()))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 수입내역입니다.");
    }

    @Test
    @DisplayName("카테고리별 지출 리스트를 조회한다.")
    @WithMockUser(username = "test@test.com")
    void getCategorySpendList() {
        // given
        UserEntity user = userDetails.user();
        Repeat repeat = new Repeat(Frequency.MONTHLY, 1, 25);

        AccountBookSpendRequest foodSpend1 = new AccountBookSpendRequest(
                "햄버거", Category.FOOD, 8000, "점심", LocalDateTime.of(2025, 4, 10, 12, 0), repeat);

        AccountBookSpendRequest foodSpend2 = new AccountBookSpendRequest(
                "핫도그", Category.FOOD, 3000, "야식", LocalDateTime.of(2025, 5, 31, 0, 0), repeat);

        AccountBookSpendRequest snackSpend = new AccountBookSpendRequest(
                "초콜릿", Category.CAFE_SNACK, 2000, "디저트", LocalDateTime.of(2025, 4, 10, 15, 0), repeat);

        accountBookRepository.save(foodSpend1.toEntity(user));
        accountBookRepository.save(foodSpend2.toEntity(user));
        accountBookRepository.save(snackSpend.toEntity(user));

        // when
        List<AccountBookSpendResponse> foodResponses = accountBookService.getCategorySpendList(Category.FOOD, user.getId());

        // then
        assertThat(foodResponses).hasSize(2);
        assertThat(foodResponses.get(0).title()).isEqualTo("핫도그");
        assertThat(foodResponses.get(0).category()).isEqualTo(Category.FOOD);
        assertThat(foodResponses.get(1).title()).isEqualTo("햄버거");
        assertThat(foodResponses.get(1).category()).isEqualTo(Category.FOOD);
    }
}
