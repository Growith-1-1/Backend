package dev.book.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.book.challenge.dto.request.ChallengeCreateRequest;
import dev.book.challenge.dto.request.ChallengeUpdateRequest;
import dev.book.challenge.dto.response.*;
import dev.book.challenge.service.ChallengeService;
import dev.book.challenge.type.Release;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.user.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static dev.book.challenge.type.Release.PUBLIC;
import static dev.book.challenge.type.Status.RECRUITING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChallengeController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChallengeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChallengeService challengeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long challengeId = 1L;
    UserEntity creator;

    @BeforeEach
    void setUP() {
        creator = UserEntity.builder()
                .name("작성자")
                .email("이메일")
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(creator);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    private ChallengeCreateRequest createRequest() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 2, 1);
        return new ChallengeCreateRequest("제목", "내용", "PUBLIC", 1000, 5, List.of("SHOPPING"), start, end);
    }

    @Test
    @DisplayName("챌린지 생성 요청 시 201 Created 와 챌린지 정보가 반환된다")
    void createChallenge() throws Exception {

        //given
        ChallengeCreateRequest challengeCreateRequest = createRequest();
        ChallengeCreateResponse challengeCreateResponse = new ChallengeCreateResponse(
                1L, "제목", "내용", PUBLIC,
                1000, 5, List.of(new ChallengeCreateResponse.CategoryDto("SHOPING")), RECRUITING,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(challengeService.createChallenge(any(), any())).willReturn(challengeCreateResponse);

        // when
        // then
        mockMvc.perform(post("/api/v1/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(challengeCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.text").value("내용"));
    }


    @Test
    @DisplayName("챌린지 목록 조회 요청 시 200 OK 와 챌린지 목록이 반환된다")
    void searchChallenge() throws Exception {
        // given
        ChallengeReadResponse response1 = new ChallengeReadResponse(
                1L, "제목", 10, 1, RECRUITING
        );

        ChallengeReadResponse response2 = new ChallengeReadResponse(
                2L, "제목", 5, 1, RECRUITING
        );

        List<ChallengeReadResponse> list = List.of(response1, response2);
        Page<ChallengeReadResponse> pageResponse = new PageImpl<>(list);

        given(challengeService.searchChallenge("제목", "내용", 1, 10))
                .willReturn(pageResponse);

        // when
        // then
        mockMvc.perform(get("/api/v1/challenges")
                        .param("title", "제목")
                        .param("text", "내용")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("제목"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].title").value("제목"));
    }

    @Test
    @DisplayName("챌린지 단건 조회 요청 시 200 OK 와 챌린지 상세정보를 반환한다")
    void searchChallengeById() throws Exception {
        // given
        ChallengeReadDetailResponse response = new ChallengeReadDetailResponse(
                1L, "작성자", "제목", Release.PUBLIC, 1000, 10, 1,
                List.of(new ChallengeReadDetailResponse.CategoryDto("SHOPPING")),
                RECRUITING,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(challengeService.searchChallengeById(any())).willReturn(response);

        // when
        // then
        mockMvc.perform(get("/api/v1/challenges/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("제목"));
    }


    @Test
    @DisplayName("챌린지 수정 요청 시 200 OK 와 수정된 챌린지 정보를 반환한다")
    void updateChallenge() throws Exception {
        // given
        ChallengeUpdateRequest request = new ChallengeUpdateRequest(
                "수정된 제목", "수정된 내용", "PUBLIC", 2000, 20, List.of("SHOPPING"),
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)
        );

        ChallengeUpdateResponse response = new ChallengeUpdateResponse(1L, "수정된 제목", "수정된 내용", Release.PUBLIC, 2000,
                20,
                List.of(new ChallengeUpdateResponse.CategoryDto("SHOPPING")),
                RECRUITING,
                LocalDate.of(2024, 5, 1),
                LocalDate.of(2024, 5, 31),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(challengeService.updateChallenge(any(), any(), any())).willReturn(response);

        // when
        // then
        mockMvc.perform(put("/api/v1/challenges/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.amount").value(2000));
    }

    @Test
    @DisplayName("챌린지 삭제 요청 시 200 OK 를 반환한다")
    void deleteChallenge() throws Exception {
        mockMvc.perform(delete("/api/v1/challenges/{id}", challengeId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("챌린지 참여 요청 시 200 OK 와 성공 메시지를 반환한다")
    void participateChallenge() throws Exception {
        mockMvc.perform(post("/api/v1/challenges/{id}/participation", challengeId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("참여가 완료 되었습니다"));
    }

    @Test
    @DisplayName("챌린지 탈퇴 요청 시 200 OK 와 성공 메시지를 반환한다")
    void leaveChallenge() throws Exception {
        mockMvc.perform(delete("/api/v1/challenges/{id}/exit", challengeId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("챌린지에서 나갔습니다"));
    }

    @Test
    @DisplayName("인기 챌린지 조회 시 200 OK 와 인기 챌린지 정보를 반환한다. ")
    void searchTopChallenge() throws Exception {

        // given
        ChallengeTopResponse response = new ChallengeTopResponse(1L, "제목", 5, 40, RECRUITING);

        given(challengeService.findTopChallenge()).willReturn(List.of(response));
        mockMvc.perform(get("/api/v1/challenges/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("제목"))
                .andExpect(jsonPath("$[0].capacity").value(5))
                .andExpect(jsonPath("$[0].participants").value(40))
                .andExpect(jsonPath("$[0].status").value("RECRUITING"));

    }

    @Test
    @DisplayName("신규 챌린지 조회 시 200 OK 와 신규 챌린지 정보를 반환한다. ")
    void searchNewChallenge() throws Exception {

        // given
        ChallengeReadResponse response = new ChallengeReadResponse(1L, "제목", 5, 40, RECRUITING);

        given(challengeService.findNewChallenge(anyInt(),anyInt())).willReturn(List.of(response));
        mockMvc.perform(get("/api/v1/challenges/new"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("제목"))
                .andExpect(jsonPath("$[0].capacity").value(5))
                .andExpect(jsonPath("$[0].participants").value(40))
                .andExpect(jsonPath("$[0].status").value("RECRUITING"));

    }

    @Test
    @DisplayName("내 챌린지 조회 시 200 OK 와 내 챌린지 정보를 반환한다. ")
    void searchMyChallenge() throws Exception {

        // given
        ChallengeParticipantResponse challengeParticipantResponse = new ChallengeParticipantResponse(
                1L, "제목", 5000, 50000, 3, false, false);

        given(challengeService.findMyChallenge(any(), anyInt(), anyInt())).willReturn(List.of(challengeParticipantResponse));

        mockMvc.perform(get("/api/v1/challenges/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("제목"))
                .andExpect(jsonPath("$[0].totalSpend").value(5000))
                .andExpect(jsonPath("$[0].amount").value(50000))
                .andExpect(jsonPath("$[0].endDay").value(3))
                .andExpect(jsonPath("$[0].isSuccess").value(false))
                .andExpect(jsonPath("$[0].isWriteTip").value(false));
    }
}