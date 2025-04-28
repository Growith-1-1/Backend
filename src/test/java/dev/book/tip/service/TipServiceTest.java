package dev.book.tip.service;

import dev.book.challenge.entity.Challenge;
import dev.book.challenge.exception.ChallengeException;
import dev.book.challenge.exception.ErrorCode;
import dev.book.challenge.user_challenge.entity.UserChallenge;
import dev.book.challenge.user_challenge.repository.UserChallengeRepository;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.global.config.security.jwt.JwtAuthenticationToken;
import dev.book.global.sse.service.SseService;
import dev.book.tip.dto.request.TipRequest;
import dev.book.tip.dto.response.TipResponse;
import dev.book.tip.entity.Tip;
import dev.book.tip.exception.TipErrorException;
import dev.book.tip.repository.TipRepository;
import dev.book.user.entity.UserEntity;
import dev.book.util.UserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipServiceTest {

    @Mock
    TipRepository tipRepository;

    @Mock
    UserChallengeRepository userChallengeRepository;

    @Mock
    SseService sseService;

    @InjectMocks
    TipService tipService;

    CustomUserDetails userDetails;
    Challenge challenge;
    UserChallenge userChallenge;

    @BeforeEach
    public void createUser(){
        UserEntity user = UserBuilder.of();
        ReflectionTestUtils.setField(user, "id", 1L);
        userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(userDetails, userDetails.getAuthorities()));

        challenge = mock(Challenge.class);
        ReflectionTestUtils.setField(challenge, "id", 1L);
        userChallenge = UserChallenge.of(userDetails.user(), challenge);
    }

    @Test
    @DisplayName("팁을 생성한다.")
    void createTip() {
        //given
        given(userChallengeRepository.findByUserIdAndChallengeId(anyLong(), anyLong())).willReturn(Optional.of(userChallenge));

        TipRequest tipRequest = new TipRequest(challenge.getId(), "가끔은 대중교통도 좋아요!");
        Tip tip = new Tip(tipRequest.content(), userDetails.user());
        given(tipRepository.save(any(Tip.class))).willReturn(tip);

        //when
        tipService.createTip(userDetails, tipRequest);

        //then
        verify(sseService).sendTipToAllUsers(any());
    }

    @Test
    @DisplayName("이미 팁을 작성한 챌린지라면 에러가 발생한다.")
    void alreadyCreateTipInChallenge() {
        //given
        userChallenge.writeTip(); //이미 팁을 작성하였음
        given(userChallengeRepository.findByUserIdAndChallengeId(anyLong(), anyLong())).willReturn(Optional.of(userChallenge));

        TipRequest tipRequest = new TipRequest(challenge.getId(), "가끔은 대중교통도 좋아요!");

        //when, then
        assertThatThrownBy(() -> tipService.createTip(userDetails, tipRequest))
                .isInstanceOf(TipErrorException.class)
                .hasMessageContaining("이미 팁이 작성된 챌린지입니다.");
        verify(sseService, never()).sendTipToAllUsers(any());
    }

    @Test
    @DisplayName("유저가 참여한 챌린지에 대하여 찾을 수 없을 경우 에러가 발생한다.")
    void notFoundUserChallenge() {
        //given
        given(userChallengeRepository.findByUserIdAndChallengeId(anyLong(), anyLong())).willThrow(new ChallengeException(ErrorCode.CHALLENGE_NOT_FOUND_USER));
        TipRequest tipRequest = new TipRequest(challenge.getId(), "가끔은 대중교통도 좋아요!");

        //when, then
        assertThatThrownBy(() -> tipService.createTip(userDetails, tipRequest))
                .isInstanceOf(ChallengeException.class)
                .hasMessageContaining("챌린지에 속해있지 않은 사용자 입니다.");
        verify(tipRepository,never()).save(any());
        verify(sseService, never()).sendTipToAllUsers(any());
    }

    @Test
    void getTips() {
        //given
        String content = "가끔은 대중교통도 좋아요!";
        List<Tip> tips = List.of(new Tip(content, userDetails.user()));
        given(tipRepository.find20RandomTips()).willReturn(tips);

        //when
        List<TipResponse> tipResponses = tipService.getTips();

        //then
        assertThat(tipResponses.get(0).content()).isEqualTo(content);
        assertThat(tipResponses.get(0).writer()).isEqualTo(userDetails.user().getNickname());
    }
}