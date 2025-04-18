package dev.book.accountbook.service;

import dev.book.accountbook.dto.response.AccountBookConsumeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendResponse;
import dev.book.accountbook.dto.response.AccountBookStatResponse;
import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.type.Category;
import dev.book.accountbook.type.Frequency;
import dev.book.accountbook.type.PeriodRange;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.global.config.security.jwt.JwtAuthenticationToken;
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
    private StatService statService;

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
    }

    @Test
    @DisplayName("가장 많이 소비한 카테고리 3개를 가져온다.")
    @WithMockUser(username = "test@test.com")
    void statList() {
        // given
        Long userId = 1L;

        // when
        List<AccountBookStatResponse> result = statService.statList(userId, Frequency.WEEKLY);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("category")
                .containsExactlyInAnyOrder(Category.TRANSPORTATION, Category.ALCOHOL_ENTERTAINMENT, Category.HOBBY);
    }

    @Test
    @DisplayName("카테고리별 소비항목을 가져온다.")
    @WithMockUser(username = "test@test.com")
    void categoryList() {
        // given
        Long userId = 1L;

        // when
        List<AccountBookSpendResponse> result1 = statService.categoryList(userId, Frequency.WEEKLY, Category.HOBBY);
        List<AccountBookSpendResponse> result2 = statService.categoryList(userId, Frequency.WEEKLY, Category.FOOD);
        List<AccountBookSpendResponse> result3 = statService.categoryList(userId, Frequency.WEEKLY, Category.TRANSPORTATION);

        // then
        assertThat(result1).hasSize(1);
        assertThat(result2).hasSize(2);
        assertThat(result3).hasSize(3);
    }

    @Test
    @DisplayName("주기별 절약금액을 가져온다.")
    @WithMockUser(username = "test@test.com")
    void consume() {
        // given
        Long userId = 1L;
        Frequency frequency = Frequency.WEEKLY;
        PeriodRange period = frequency.calcPeriod();
        Category category = Category.TRANSPORTATION;

        // when
        AccountBookConsumeResponse result = statService.consume(userId, frequency, category);

        // then
        assertThat(-4000).isEqualTo(result.consume());
    }
}
