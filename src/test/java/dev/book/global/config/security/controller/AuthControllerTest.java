package dev.book.global.config.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.book.accountbook.type.Category;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.global.config.security.dto.TokenDto;
import dev.book.global.config.security.jwt.JwtAuthenticationToken;
import dev.book.global.config.security.jwt.JwtUtil;
import dev.book.user.dto.request.UserSignUpRequest;
import dev.book.user.entity.UserEntity;
import dev.book.user.repository.UserRepository;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HttpServletResponse response;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
    }

    @AfterEach
    public void cleanUp() {
        SecurityContextHolder.clearContext();
        userRepository.deleteAll();
    }

    CustomUserDetails userDetails;

    @BeforeEach
    void setSecurityContext() {
        UserEntity user = userRepository.save(UserEntity.builder()
                .email("test@test.com")
                .nickname("")
                .profileImageUrl("profile")
                .name("kmg")
                .build());
        userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(userDetails, userDetails.getAuthorities()));
    }

    @WithMockUser(username = "test@test.com")
    @DisplayName("signUp : 회원가입을 하고 토큰을 얻는다")
    @Test
    public void signUp() throws Exception {

        final String url = "/api/v1/auth/signup";

        final ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserSignUpRequest( "nickname", List.of(Category.HOBBY)))));

        result.andExpect(status().isCreated());

        final Optional<UserEntity> user = userRepository.findByEmail("test@test.com");

        assertThat(user.get().getNickname()).isEqualTo("nickname");

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

        assertNotNull(accessToken, "액세스 토큰 없음");
        assertNotNull(refreshToken, "리프레시 토큰 없음");
    }


    @WithMockUser(username = "test@test.com")
    @DisplayName("logout : 로그아웃을 하고 토큰을 삭제한다")
    @Test
    public void logout() throws Exception {
        final String url = "/api/v1/auth/logout";

        final ResultActions result = mockMvc.perform(get(url));

        result.andExpect(status().isOk());

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

    @WithMockUser(username = "test@test.com")
    @DisplayName("reissue: refreshToken 으로 토큰을 재발급한다.")
    @Test
    public void reissue() throws Exception {

        final String url = "/api/v1/auth/reissue";

        Authentication authentication = new JwtAuthenticationToken(userDetails, userDetails.getAuthorities());
        TokenDto tokenDto = jwtUtil.generateToken(response, authentication);

        Thread.sleep(1000);

        final ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new TokenDto(null, tokenDto.refreshToken()))));

        result.andExpect(status().isOk());

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

        assertNotNull(accessToken, "액세스 토큰 재발급");
        assertNotNull(refreshToken, "리프레시 토큰 재발급");
        assertThat(refreshToken).isNotEqualTo(tokenDto.refreshToken());
    }

    @WithMockUser(username = "test@test.com")
    @DisplayName("validationTest : 토큰의 유효성 검사")
    @Test
    public void validationTest() {

        Authentication authentication = new JwtAuthenticationToken(userDetails, userDetails.getAuthorities());
        TokenDto tokenDto = jwtUtil.generateToken(response, authentication);

        assertThat(jwtUtil.validateToken(tokenDto.accessToken())).isEqualTo(authentication.getName());
        assertThat(jwtUtil.validateToken(tokenDto.refreshToken())).isEqualTo(authentication.getName());

        assertThrows(SignatureException.class, () -> {
            jwtUtil.validateToken(tokenDto.accessToken() + "a");
        });

        assertThrows(SignatureException.class, () -> {
            jwtUtil.validateToken(tokenDto.refreshToken() + "a");
        });
    }

}