package dev.book.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.global.config.security.jwt.JwtAuthenticationToken;
import dev.book.user.dto.request.UserProfileUpdateRequest;
import dev.book.user.entity.UserEntity;
import dev.book.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;


    @BeforeEach
    public void mockMvcSetUp(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
    }

    static CustomUserDetails userDetails;

    String nickname = "nickname";
    String email = "test@test.com";
    String profile = "profile";

    @BeforeEach
    public void createUser(){
        UserEntity user = userRepository.save(UserEntity.builder()
                .email("test@test.com")
                .name("test")
                .nickname("nickname")
                .profileImageUrl("profile").build());
        userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(userDetails, userDetails.getAuthorities()));
    }

    @AfterEach
    public void cleanUp(){
        userRepository.deleteAll();
    }

    @DisplayName("getUserProfile : 유저의 프로필 반환")
    @WithMockUser(username = "test@test.com")
    @Test
    void getUserProfile() throws Exception {

        final String url = "/users/profile";

        final ResultActions result = mockMvc.perform(get(url));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value(nickname))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.profileImageUrl").value(profile));

    }

    @DisplayName("updateUserProfile : 유저의 프로필을 변경한다.")
    @WithMockUser(username = "test@test.com")
    @Test
    void updateUserProfile() throws Exception {

        final String url = "/users/profile";

        final ResultActions result = mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        objectMapper.writeValueAsString(
                                new UserProfileUpdateRequest("testNickname", "testProfile"))));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("testNickname"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.profileImageUrl").value("testProfile"));

    }

    @DisplayName("deleteUser : 유저를 삭제한다.")
    @WithMockUser(username = "test@test.com")
    @Test
    void deleteUser() throws Exception {
        final String url = "/users";

        final ResultActions result = mockMvc.perform(delete(url));

        result.andExpect(status().isOk());

        final Optional<UserEntity> user = userRepository.findByEmail(userDetails.getUsername());

        assertThat(user.isEmpty());

        // 응답 쿠키 가져오기
        MockHttpServletResponse response = result.andReturn().getResponse();

        String accessToken = Arrays.stream(response.getCookies())
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        String refreshToken = Arrays.stream(response.getCookies())
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        assertNull(accessToken, "액세스 토큰이 삭제되지 않음");
        assertNull(refreshToken, "리프레시 토큰이 삭제되지 않음");

    }
}