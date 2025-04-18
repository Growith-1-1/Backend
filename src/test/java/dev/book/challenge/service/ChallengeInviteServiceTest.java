package dev.book.challenge.service;

import dev.book.challenge.challenge_invite.repository.ChallengeInviteRepository;
import dev.book.challenge.dto.request.ChallengeCreateRequest;
import dev.book.challenge.dto.request.ChallengeInviteRequest;
import dev.book.challenge.entity.Challenge;
import dev.book.challenge.exception.ChallengeException;
import dev.book.challenge.repository.ChallengeRepository;
import dev.book.challenge.user_challenge.repository.UserChallengeRepository;
import dev.book.user.entity.UserEntity;
import dev.book.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChallengeInviteServiceTest {
    @InjectMocks
    private ChallengeInviteService challengeInviteService;
    @Mock
    private ChallengeInviteRepository challengeInviteRepository;
    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private UserChallengeRepository userChallengeRepository;
    @Mock
    private UserRepository userRepository;

    private ChallengeCreateRequest createRequest() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 2, 1);
        return new ChallengeCreateRequest("제목", "내용", "PUBLIC", 1000, 5, "NONE", start, end);
    }

    @Test
    @DisplayName("챌린지에 초대할수 있다.")
    void inviteChallenge() {

        // given
        ChallengeCreateRequest challengeCreateRequest = createRequest();
        UserEntity requestUser = UserEntity.builder()
                .name("초대하는 사람")
                .email("이메일1")
                .build();
        ChallengeInviteRequest challengeInviteRequest = new ChallengeInviteRequest("이메일2");
        UserEntity InviteUser = UserEntity.builder()
                .name("초대받는 사람")
                .email("이메일2")
                .build();

        given(userRepository.findByEmail(any())).willReturn(Optional.of(InviteUser));

        Challenge challenge = Challenge.of(challengeCreateRequest, requestUser);
        given(challengeRepository.findById(any())).willReturn(Optional.of(challenge));
        given(userChallengeRepository.existsByUserIdAndChallengeId(any(), any())).willReturn(true);

        // when
        challengeInviteService.invite(1L, requestUser, challengeInviteRequest);
        //then
        verify(userRepository).findByEmail("이메일2");
        verify(challengeRepository).findById(1L);
        verify(challengeInviteRepository).save(any());


    }

    @Test
    @DisplayName("챌린지에 속해있지 않으면 초대할수 없다.")
    void inviteInNotChallenge() {

        // given
        ChallengeCreateRequest challengeCreateRequest = createRequest();
        UserEntity requestUser = UserEntity.builder()
                .name("초대하는 사람")
                .email("이메일1")
                .build();
        ChallengeInviteRequest challengeInviteRequest = new ChallengeInviteRequest("이메일2");
        UserEntity InviteUser = UserEntity.builder()
                .name("초대받는 사람")
                .email("이메일2")
                .build();

        given(userRepository.findByEmail(any())).willReturn(Optional.of(InviteUser));

        Challenge challenge = Challenge.of(challengeCreateRequest, requestUser);
        given(challengeRepository.findById(any())).willReturn(Optional.of(challenge));
        given(userChallengeRepository.existsByUserIdAndChallengeId(any(), any())).willReturn(false);

        // when
        //then
        assertThatThrownBy(() -> challengeInviteService.invite(1L, requestUser, challengeInviteRequest)).isInstanceOf(ChallengeException.class)
                .hasMessage("초대할 권한이 없습니다.");


    }
    // todo 테스트 추가
}