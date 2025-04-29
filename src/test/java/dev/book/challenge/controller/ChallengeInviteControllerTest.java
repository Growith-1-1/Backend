package dev.book.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.book.challenge.dto.request.ChallengeInviteRequest;
import dev.book.challenge.dto.response.ChallengeInviteResponse;
import dev.book.challenge.service.ChallengeInviteService;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.user.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChallengeInviteController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChallengeInviteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChallengeInviteService challengeInviteService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long challengeId = 1L;
    
    private UserEntity user;


    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .name("초대자")
                .email("requestUser@email.com")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }


    @Test
    @DisplayName("챌린지 초대 요청 시 200 OK 와 성공 메시지를 반환한다")
    void invite() throws Exception {
        // given
        ChallengeInviteRequest request = new ChallengeInviteRequest("inviteUser@email.com");

        // when & then
        mockMvc.perform(post("/api/v1/challenges/{id}/invites", challengeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("초대 완료 하였습니다"));
    }

    @Test
    @DisplayName("내가 받은 챌린지 초대 목록 조회 요청 시 200 OK 와 목록이 반환된다")
    void findInviteList() throws Exception {
        // given
        ChallengeInviteResponse challengeInviteResponse1 = new ChallengeInviteResponse(
                1L,
                "초대한 사람",
                "챌린지 제목1",
                false, // 아직 응답하지 않음
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );
        ChallengeInviteResponse challengeInviteResponse2 = new ChallengeInviteResponse(
                2L,
                "초대한 사람",
                "챌린지 제목2",
                false, // 아직 응답하지 않음
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );
        ;

        given(challengeInviteService.getMyInviteList(any())).willReturn(List.of(challengeInviteResponse1, challengeInviteResponse2));

        // when & then
        mockMvc.perform(get("/api/v1/challenges/invites/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].challengeName").value("챌린지 제목1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].challengeName").value("챌린지 제목2"));
    }

    @Test
    @DisplayName("챌린지 초대를 수락하면 200 OK 를 반환한다")
    void acceptInvite() throws Exception {
        mockMvc.perform(patch("/api/v1/challenges/invites/{id}/accept", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("챌린지 초대를 거절하면 200 OK 를 반환한다")
    void rejectInvite() throws Exception {
        mockMvc.perform(patch("/api/v1/challenges/invites/{id}/reject", 1L))
                .andExpect(status().isOk());
    }
}