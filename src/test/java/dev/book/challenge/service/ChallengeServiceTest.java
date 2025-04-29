package dev.book.challenge.service;

import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.challenge.dto.request.ChallengeCreateRequest;
import dev.book.challenge.dto.request.ChallengeUpdateRequest;
import dev.book.challenge.dto.response.ChallengeCreateResponse;
import dev.book.challenge.dto.response.ChallengeReadDetailResponse;
import dev.book.challenge.dto.response.ChallengeReadResponse;
import dev.book.challenge.dto.response.ChallengeUpdateResponse;
import dev.book.challenge.entity.Challenge;
import dev.book.challenge.exception.ChallengeException;
import dev.book.challenge.repository.ChallengeRepository;
import dev.book.challenge.user_challenge.repository.UserChallengeRepository;
import dev.book.global.entity.Category;
import dev.book.global.repository.CategoryRepository;
import dev.book.user.entity.UserEntity;
import dev.book.user.repository.UserRepository;
import dev.book.util.UserBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static dev.book.challenge.exception.ErrorCode.CHALLENGE_CAPACITY_FULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @InjectMocks
    private ChallengeService challengeService;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private UserChallengeRepository userChallengeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AccountBookRepository accountBookRepository;

    private ChallengeCreateRequest createRequest() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 2, 1);
        return new ChallengeCreateRequest("제목", "내용", "PUBLIC", 1000, 5, List.of("SHOPPING"), start, end);
    }

    @Test
    @DisplayName("챌린지를 등록할수 있다.")
    void createChallenge() {

        // given
        ChallengeCreateRequest challengeCreateRequest = createRequest();
        UserEntity creator = UserBuilder.of("이메일", "사용자");
        Challenge challenge = Challenge.of(challengeCreateRequest, creator);
        given(challengeRepository.save(any())).willReturn(challenge);
        given(userRepository.findByEmail(any())).willReturn(Optional.of(creator));
        Category category = new Category("SHOPPING", "쇼핑");
        given(categoryRepository.findByCategoryIn(any())).willReturn(List.of(category));

        // when
        ChallengeCreateResponse response = challengeService.createChallenge(creator, challengeCreateRequest);
        //then
        assertThat(response.title()).isEqualTo("제목");


    }

    @Test
    @DisplayName("챌린지를 조회 할수 있다.")
    void searchChallenge() {

        // given
        ChallengeCreateRequest challengeCreateRequest = createRequest();
        UserEntity creator = UserBuilder.of("이메일", "사용자");

        Challenge challenge = Challenge.of(challengeCreateRequest, creator);
        ChallengeReadResponse challengeReadResponse = ChallengeReadResponse.fromEntity(challenge);
        Pageable pageRequest = PageRequest.of(0, 10);
        Page<ChallengeReadResponse> mockPage = new PageImpl<>(List.of(challengeReadResponse), pageRequest, 1);
        given(challengeRepository.search(anyString(), anyString(), any())).willReturn(mockPage);

        // when
        Page<ChallengeReadResponse> response = challengeService.searchChallenge("제목", "내용", 1, 10);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent().get(0).title()).isEqualTo("제목");
    }

    @Test
    @DisplayName("챌린지를 상세하게 조회 할수 있다.")
    void searchChallengeById() {

        // given
        ChallengeCreateRequest challengeCreateRequest = createRequest();
        UserEntity creator = UserBuilder.of("이메일", "사용자");

        Challenge challenge = Challenge.of(challengeCreateRequest, creator);
        given(challengeRepository.findWithCreatorById(any())).willReturn(Optional.of(challenge));

        // when
        ChallengeReadDetailResponse response = challengeService.searchChallengeById(1L);

        // then
        assertThat(response.title()).isEqualTo("제목");
    }

    @Test
    @DisplayName("존재하지 않는 챌린지는 조회할수 없다.")
    void notSearchChallengeById() {

        // given
        given(challengeRepository.findWithCreatorById(any())).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> challengeService.searchChallengeById(99L)).isInstanceOf(ChallengeException.class);
    }

    @Test
    @DisplayName("챌린지를 수정 할수 있다.")
    void updateChallenge() {

        // given
        ChallengeCreateRequest challengeCreateRequest = createRequest();
        UserEntity creator = UserBuilder.of("이메일", "사용자");

        Challenge challenge = Challenge.of(challengeCreateRequest, creator);
        given(challengeRepository.findByIdAndCreatorId(any(), any())).willReturn(Optional.of(challenge));

        LocalDate start = LocalDate.of(2024, 2, 1);
        LocalDate end = LocalDate.of(2024, 3, 1);
        ChallengeUpdateRequest updateRequest = new ChallengeUpdateRequest("수정", "수정", "PUBLIC", 1000, 5, List.of("SHOPPING"), start, end);

        //when
        ChallengeUpdateResponse challengeUpdateResponse = challengeService.updateChallenge(creator, 1L, updateRequest);

        //then
        assertThat(challenge.getTitle()).isEqualTo("수정");
        assertThat(challengeUpdateResponse.title()).isEqualTo("수정");
    }

    @Test
    @DisplayName("만든 사람만 챌린지를 수정 할수 있다.")
    void updateNotChallenge() {

        //given
        UserEntity creator = UserBuilder.of("이메일1", "작성자");

        UserEntity noCreator = UserBuilder.of("이메일2", "사용자");

        given(challengeRepository.findByIdAndCreatorId(1L, noCreator.getId())).willReturn(Optional.empty());

        LocalDate start = LocalDate.of(2024, 2, 1);
        LocalDate end = LocalDate.of(2024, 3, 1);
        ChallengeUpdateRequest updateRequest = new ChallengeUpdateRequest("수정", "수정", "PUBLIC", 1000, 5, List.of("SHOPPING"), start, end);

        //when
        //then
        assertThatThrownBy(() -> challengeService.updateChallenge(noCreator, 1L, updateRequest)).isInstanceOf(ChallengeException.class)
                .hasMessage("수정 및 삭제 권한이 없습니다.");
    }

    @Test
    @DisplayName("챌린지를 삭제 할수 있다.")
    void deleteNotChallenge() {
        //given
        ChallengeCreateRequest challengeCreateRequest = createRequest();
        UserEntity creator = UserBuilder.of("이메일1", "작성자");

        Challenge challenge = Challenge.of(challengeCreateRequest, creator);
        given(userRepository.findByEmail(any())).willReturn(Optional.of(creator));
        given(challengeRepository.findByIdAndCreatorId(any(), any())).willReturn(Optional.of(challenge));

        //when
        challengeService.deleteChallenge(creator, 1L);

        //then
        verify(challengeRepository, times(1)).delete(challenge);
    }

    @Test
    @DisplayName("만든 사람만 챌린지를 삭제 할수 있다.")
    void deleteChallenge() {
        //given
        UserEntity noCreator = UserBuilder.of("이메일2", "사용자");

        given(userRepository.findByEmail(any())).willReturn(Optional.of(noCreator));

        given(challengeRepository.findByIdAndCreatorId(1L, noCreator.getId())).willReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> challengeService.deleteChallenge(noCreator, 1L)).isInstanceOf(ChallengeException.class)
                .hasMessage("수정 및 삭제 권한이 없습니다.");
    }


    @Test
    @DisplayName("사용자는 챌린지를 참여할수 있다.")
    void participate() {
        // given
        ChallengeCreateRequest challengeCreateRequest = createRequest();
        UserEntity creator = UserBuilder.of("이메일1", "작성자");
        UserEntity noCreator = UserBuilder.of("이메일2", "사용자");

        Challenge challenge = Challenge.of(challengeCreateRequest, creator);
        given(userRepository.findByEmail(any())).willReturn(Optional.of(noCreator));
        given(challengeRepository.findByIdWithLock(any())).willReturn(Optional.of(challenge));
        // when
        challengeService.participateWithLock(noCreator, 1L);
        // then
        verify(userChallengeRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("사용자는 참여한 챌린지를 참여할수 없다.")
    void NotParticipate() {
        //given
        ChallengeCreateRequest challengeCreateRequest = createRequest();
        UserEntity user = UserBuilder.of("이메일1", "사용자");
        Challenge challenge = Challenge.of(challengeCreateRequest, user);

        given(userRepository.findByEmail(any())).willReturn(Optional.of(user));
        given(challengeRepository.findByIdWithLock(any())).willReturn(Optional.of(challenge));
        given(userChallengeRepository.existsByUserIdAndChallengeId(any(), any())).willReturn(true);
        // when
        // then
        assertThatThrownBy(() -> challengeService.participateWithLock(user, 1L)).isInstanceOf(ChallengeException.class)
                .hasMessage("이미 참여된 챌린지 입니다.");

    }

    @Test
    @DisplayName("모집인원 초과한 챌린지는 참여할수 없다.")
    void fullParticipate() {
        // given
        Challenge challenge = mock(Challenge.class);

        UserEntity noCreator = UserBuilder.of("이메일1", "사용자");
        given(userRepository.findByEmail(any())).willReturn(Optional.of(noCreator));
        given(challengeRepository.findByIdWithLock(any())).willReturn(Optional.of(challenge));
        given(userChallengeRepository.existsByUserIdAndChallengeId(any(), any())).willReturn(false);
        doThrow(new ChallengeException(CHALLENGE_CAPACITY_FULL))
                .when(challenge).isParticipantsMoreThanCapacity();
        //when
        // then
        assertThatThrownBy(() -> challengeService.participateWithLock(noCreator, 1L)).isInstanceOf(ChallengeException.class)
                .hasMessage("참여 인원이 초과 하였습니다.");

    }
}