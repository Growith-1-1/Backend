package dev.book.challenge.service;

import dev.book.achievement.achievement_user.dto.event.InviteFriendToChallengeEvent;
import dev.book.challenge.challenge_invite.entity.ChallengeInvite;
import dev.book.challenge.challenge_invite.repository.ChallengeInviteRepository;
import dev.book.challenge.dto.request.ChallengeInviteRequest;
import dev.book.challenge.dto.response.ChallengeInviteResponse;
import dev.book.challenge.entity.Challenge;
import dev.book.challenge.exception.ChallengeException;
import dev.book.challenge.repository.ChallengeRepository;
import dev.book.challenge.user_challenge.entity.UserChallenge;
import dev.book.challenge.user_challenge.repository.UserChallengeRepository;
import dev.book.user.entity.UserEntity;
import dev.book.user.exception.UserErrorException;
import dev.book.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.book.challenge.exception.ErrorCode.*;
import static dev.book.user.exception.UserErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ChallengeInviteService {

    private final ChallengeInviteRepository challengeInviteRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void invite(Long challengeId, UserEntity user, ChallengeInviteRequest challengeInviteRequest) {

        Challenge challenge = getChallenge(challengeId);
        UserEntity inviteUser = userRepository.findByEmail(challengeInviteRequest.email()).orElseThrow(() -> new UserErrorException(USER_NOT_FOUND));

        isNotParticipant(challengeId, user);// 자신이 참가하지않은 챌린지에 초대 하는 상황
        isAlreadyInvited(inviteUser, challenge); // 이미 초대가 된 상황일때

        challenge.isParticipantsMoreThanCapacity();
        ChallengeInvite challengeInvite = ChallengeInvite.of(user, inviteUser, challenge);
        challengeInviteRepository.save(challengeInvite);

        eventPublisher.publishEvent(new InviteFriendToChallengeEvent(user));

    }

    @Transactional(readOnly = true)
    public List<ChallengeInviteResponse> getMyInviteList(UserEntity user) {

        List<ChallengeInvite> challengeInvites = challengeInviteRepository.findAllByInviteUserId(user.getId());
        return challengeInvites.stream().map(ChallengeInviteResponse::fromEntity).toList();
    }

    @Transactional
    public void acceptInvite(Long inviteId, UserEntity user) {

        ChallengeInvite challengeInvite = challengeInviteRepository.findByIdAndInviteUserId(inviteId, user.getId()).orElseThrow(() -> new ChallengeException(CHALLENGE_NOT_FOUND_INVITED));
        challengeInvite.accept();
        user.plusParticipatingChallenge();
        challengeInviteRepository.delete(challengeInvite);

        UserChallenge userChallenge = UserChallenge.of(user, challengeInvite.getChallenge());
        userChallengeRepository.save(userChallenge);
    }

    @Transactional
    public void rejectInvite(Long inviteId, UserEntity user) {

        ChallengeInvite challengeInvite = challengeInviteRepository.findByIdAndInviteUserId(inviteId, user.getId()).orElseThrow(() -> new ChallengeException(CHALLENGE_NOT_FOUND_INVITED));
        challengeInvite.reject();
        challengeInviteRepository.delete(challengeInvite);
    }

    private Challenge getChallenge(Long challengeId) {
        return challengeRepository.findById(challengeId).orElseThrow(() -> new ChallengeException(CHALLENGE_NOT_FOUND));
    }

    private void isAlreadyInvited(UserEntity inviteUser, Challenge challenge) {
        if (challengeInviteRepository.existsByInviteUserIdAndChallengeId(inviteUser.getId(), challenge.getId())) {
            throw new ChallengeException(CHALLENGE_ALREADY_INVITED);
        }
    }

    private void isNotParticipant(Long challengeId, UserEntity user) {
        if (!userChallengeRepository.existsByUserIdAndChallengeId(user.getId(), challengeId)) {
            throw new ChallengeException(CHALLENGE_INVITE_INVALID);
        }
    }
}
