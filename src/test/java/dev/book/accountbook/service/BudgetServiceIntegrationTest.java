package dev.book.accountbook.service;

import dev.book.accountbook.dto.request.BudgetRequest;
import dev.book.accountbook.dto.response.BudgetResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.entity.Budget;
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
import dev.book.user.exception.UserErrorException;
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
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BudgetServiceIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountBookRepository accountBookRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private BudgetService budgetService;

    static CustomUserDetails userDetails;
    static List<AccountBook> savedBooks = null;
    static Budget savedBudget = null;

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

        savedBudget = budgetRepository.save(new Budget(100000, 4, user));
    }

    @AfterEach
    public void cleanUp() {
        accountBookRepository.deleteAllInBatch();
        budgetRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("예산을 등록할 수 있다.")
    @WithMockUser(username = "test@test.com")
    void createBudget() {
        // given
        UserEntity user = UserEntity.builder()
                .email("test@example.com")
                .name("홍길동")
                .nickname("길동이")
                .profileImageUrl("test")
                .build();
        userRepository.save(user);

        BudgetRequest request = new BudgetRequest(10000);

        // when
        BudgetResponse response = budgetService.createBudget(user.getId(), request);

        // then
        assertThat(response.budget()).isEqualTo(request.budget());
    }

    @Test
    @DisplayName("예산 생성 중 유저가 없을 경우 예외가 발생한다.")
    @WithMockUser(username = "test@test.com")
    void notFoundUserByCreateBudget() {
        // given
        Long unKnownUser = 0L;

        BudgetRequest request = new BudgetRequest(10000);

        // when
        // then
        assertThatThrownBy(() -> budgetService.createBudget(unKnownUser, request))
                .isInstanceOf(UserErrorException.class)
                .hasMessage("유저를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("예산 생성 중 이미 예산이 등록된 유저일 경우 예외가 발생한다.")
    @WithMockUser(username = "test@test.com")
    void duplicateBudgetByCreateBudget() {
        // given
        UserEntity user = userDetails.user();
        BudgetRequest request = new BudgetRequest(10000);

        // when
        // then
        assertThatThrownBy(() -> budgetService.createBudget(user.getId(), request))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("이미 예산이 등록되어 있습니다.");
    }

    @Test
    @DisplayName("예산을 조회할 수 있다.")
    @WithMockUser(username = "test@test.com")
    void getBudget() {
        // given
        UserEntity user = userDetails.user();

        // when
        BudgetResponse response = budgetService.getBudget(savedBudget.getId(), user.getId());

        // then
        assertThat(response.budget()).isEqualTo(100000);
    }

    @Test
    @DisplayName("예산을 조회 중 유저를 찾지 못할 경우 예외가 발생한다.")
    @WithMockUser(username = "test@test.com")
    void notFoundUserByGetBudget() {
        // given
        Long unKnownUser = 0L;

        // when
        // then
        assertThatThrownBy(() -> budgetService.getBudget(savedBudget.getId(), unKnownUser))
                .isInstanceOf(UserErrorException.class)
                .hasMessage("유저를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("예산을 조회 중 찾는 예산이 없을 경우 예외가 발생한다.")
    @WithMockUser(username = "test@test.com")
    void notFoundBudgetByGetBudget() {
        // given
        Long unKnownBudget = 0L;
        UserEntity user = userDetails.user();

        // when
        // then
        assertThatThrownBy(() -> budgetService.getBudget(unKnownBudget, user.getId()))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 예산입니다.");
    }

    @Test
    @DisplayName("예산을 수정할 수 있다.")
    void modifyBudget() {
        // given
        UserEntity user = userDetails.user();
        BudgetRequest request = new BudgetRequest(1000);

        // when
        BudgetResponse response = budgetService.modify(user.getId(), savedBudget.getId(), request);

        // then
        assertThat(response.budget()).isEqualTo(request.budget());
    }

    @Test
    @DisplayName("예산 수정 중 찾는 예산이 없을 경우 예외가 발생한다.")
    void notFoundBudgetByModifyBudget() {
        // given
        Long unKnownBudget = 0L;
        UserEntity user = userDetails.user();
        BudgetRequest request = new BudgetRequest(1000);

        // when
        // then
        assertThatThrownBy(() -> budgetService.modify(user.getId(), unKnownBudget, request))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 예산입니다.");
    }

    @Test
    @DisplayName("예산을 삭제할 수 있다.")
    void deleteBudget() {
        // given
        UserEntity user = userDetails.user();

        // when
        budgetService.deleteBudget(user.getId(), savedBudget.getId());

        // then
        assertThat(budgetRepository.findById(savedBudget.getId())).isEmpty();
    }

    @Test
    @DisplayName("예산 삭제 중 찾는 예산이 없을 경우 예외가 발생한다.")
    void notFoundBudgetByDeleteBudget() {
        // given
        Long unKnownBudget = 0L;
        UserEntity user = userDetails.user();

        // when
        // then
        assertThatThrownBy(() -> budgetService.deleteBudget(user.getId(), unKnownBudget))
                .isInstanceOf(AccountBookErrorException.class)
                .hasMessage("존재하지 않는 예산입니다.");
    }
}
