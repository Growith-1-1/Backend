package dev.book.accountbook.service;

import dev.book.accountbook.dto.response.AccountBookConsumeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendResponse;
import dev.book.accountbook.dto.response.AccountBookStatResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.repository.AccountBookRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StatServiceIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountBookRepository accountBookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StatService statService;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
    }

    static CustomUserDetails userDetails;
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
        accountBookRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("지정한 기간에 소비한 카테고리를 전부 들고온다.")
    @WithMockUser(username = "test@test.com")
    void statList() {
        // given
        UserEntity user = userDetails.user();

        // when
        List<AccountBookStatResponse> result = statService.statList(user.getId(), Frequency.MONTHLY);

        // then
        assertThat(result).hasSize(8);
        assertThat(result).extracting("category")
                .containsExactlyInAnyOrder("식비", "카페 / 간식", "편의점 / 마트 / 잡화", "술 / 유흥", "쇼핑", "의료 / 건강 / 피트니스", "미용", "교통 / 자동차");
    }

    @Test
    @DisplayName("카테고리별 소비항목을 가져온다.")
    @WithMockUser(username = "test@test.com")
    void categoryList() {
        // given
        UserEntity user = userDetails.user();

        // when
        List<AccountBookSpendResponse> result1 = statService.categoryList(user.getId(), Frequency.MONTHLY, "cafe_snack");
        List<AccountBookSpendResponse> result2 = statService.categoryList(user.getId(), Frequency.MONTHLY, "food");
        List<AccountBookSpendResponse> result3 = statService.categoryList(user.getId(), Frequency.MONTHLY, "transportation");

        // then
        assertThat(result1).hasSize(1);
        assertThat(result2).hasSize(1);
        assertThat(result3).hasSize(1);
    }

    @Test
    @DisplayName("주기별 절약금액을 가져온다.")
    @WithMockUser(username = "test@test.com")
    void consume() {
        // given
        UserEntity user = userDetails.user();
        Frequency frequency = Frequency.MONTHLY;

        // when
        AccountBookConsumeResponse result = statService.consume(user.getId(), frequency);

        // then
        assertThat(-228900).isEqualTo(result.consume());
    }
}
