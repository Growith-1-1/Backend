package dev.book.challenge.service;

import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.type.CategoryType;
import dev.book.achievement.achievement_user.dto.event.CreateChallengeEvent;
import dev.book.challenge.ChallengeCategory;
import dev.book.challenge.dto.request.ChallengeCreateRequest;
import dev.book.challenge.dto.request.ChallengeUpdateRequest;
import dev.book.challenge.dto.response.*;
import dev.book.challenge.entity.Challenge;
import dev.book.challenge.exception.ChallengeException;
import dev.book.challenge.repository.ChallengeRepository;
import dev.book.challenge.user_challenge.entity.UserChallenge;
import dev.book.challenge.user_challenge.repository.UserChallengeRepository;
import dev.book.global.entity.Category;
import dev.book.global.repository.CategoryRepository;
import dev.book.user.entity.UserEntity;
import dev.book.user.exception.UserErrorException;
import dev.book.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static dev.book.challenge.exception.ErrorCode.*;
import static dev.book.user.exception.UserErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final AccountBookRepository accountBookRepository;

    @Transactional
    public ChallengeCreateResponse createChallenge(UserEntity user, ChallengeCreateRequest challengeCreateRequest) {
        UserEntity creator = getUser(user);
        log.info("챌린지 생성하려는 사용자 : {}", creator.getEmail());
        List<Category> categories = categoryRepository.findByCategoryIn(challengeCreateRequest.categoryList());

        Challenge challenge = Challenge.of(challengeCreateRequest, creator);
        categories.forEach(category -> new ChallengeCategory(challenge, category));

        Challenge savedChallenge = challengeRepository.save(challenge);
        UserChallenge userChallenge = UserChallenge.of(creator, savedChallenge);
        userChallengeRepository.save(userChallenge);

        eventPublisher.publishEvent(new CreateChallengeEvent(user));
        creator.plusParticipatingChallenge();
        return ChallengeCreateResponse.fromEntity(challenge);

    }
    @Transactional(readOnly = true)
    public Page<ChallengeReadResponse> searchChallenge(String title, String text, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return challengeRepository.search(title, text, pageable);
    }

    @Transactional(readOnly = true)
    public ChallengeReadDetailResponse searchChallengeById(Long id) {
        Challenge challenge = challengeRepository.findWithCreatorById(id).orElseThrow(() -> new ChallengeException(CHALLENGE_NOT_FOUND));
        return ChallengeReadDetailResponse.fromEntity(challenge);
    }

    @Transactional
    public ChallengeUpdateResponse updateChallenge(UserEntity user, Long id, ChallengeUpdateRequest challengeUpdateRequest) {
        log.info("챌린지 수정 할려고 하는 사용자 : {}, 삭제 할려고 하는 챌린지 : {}", user.getEmail(),id);
        Challenge challenge = getMyChallenge(user.getId(), id);
        List<Category> categories = categoryRepository.findByCategoryIn(challengeUpdateRequest.categoryList());
        challenge.updateInfo(challengeUpdateRequest, categories);
        challengeRepository.flush();
        return ChallengeUpdateResponse.fromEntity(challenge);
    }

    @Transactional
    public void deleteChallenge(UserEntity user, Long id) {
        log.info("챌린지 삭제 할려고 하는 사용자 : {}, 삭제 할려고 하는 챌린지 : {}", user.getEmail(), id);
        UserEntity creator = getUser(user);
        Challenge challenge = getMyChallenge(creator.getId(), id);
        log.info("삭제 할려고 하는 챌린지 : {}", user.getEmail());
        List<Long> userIds = userChallengeRepository.findUserIdByChallengeId(challenge.getId());
        List<UserEntity> users = userRepository.findAllById(userIds);

        // 챌린지가 삭제되어 user들의 참가한 챌린지 수를 감소 시킨다.
        for (UserEntity participant : users) {
            log.info("챌린지에 속해있던 유저:{}", participant.getEmail());
            participant.minusParticipatingChallenge();
        }

        challengeRepository.delete(challenge);

    }

    @Transactional
    public void participateWithLock(UserEntity user, Long id) {
        UserEntity userEntity = getUser(user);
        Challenge challenge = challengeRepository.findByIdWithLock(id).orElseThrow(() -> new ChallengeException(CHALLENGE_NOT_FOUND));
        challenge.checkAlreadyStartOrEnd();
        checkExist(user, id);

        challenge.isParticipantsMoreThanCapacity();
        challenge.plusCurrentCapacity();
        challengeRepository.flush();

        userEntity.plusParticipatingChallenge();
        UserChallenge userChallenge = UserChallenge.of(userEntity, challenge);
        userChallengeRepository.save(userChallenge);
    }

    @Transactional
    public void leaveChallenge(UserEntity user, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(() -> new ChallengeException(CHALLENGE_NOT_FOUND));
        UserEntity userEntity = getUser(user);
        checkNotExist(user, challenge.getId());

        userEntity.minusParticipatingChallenge();
        challenge.minusCurrentCapacity();
        userChallengeRepository.deleteByUserIdAndChallengeId(userEntity.getId(), challenge.getId());

    }

    public List<ChallengeTopResponse> findTopChallenge() {
        Pageable pageable = PageRequest.of(0, 10);
        return challengeRepository.findTopChallenge(pageable);
    }

    // 오늘 기준 3일전까지의 챌린지를 신규 챌린지로 본다,
    @Transactional(readOnly = true)
    public List<ChallengeReadResponse> findNewChallenge(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDateTime = now.toLocalDate().atTime(23, 59, 59, 999_999_999);// 오늘 비교시 끝 부분
        LocalDateTime startDateTime = endDateTime.minusDays(3); // 3일전이 비교시 시작부분
        return challengeRepository.findNewChallenge(pageable, startDateTime, endDateTime);
    }

    @Transactional(readOnly = true)
    public List<ChallengeParticipantResponse> findMyChallenge(UserEntity user, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        //user가 속한 챌린지들을 불러온다.
        List<UserChallenge> userChallenges = userChallengeRepository.findChallengeByUserId(user.getId(), pageable);

        List<ChallengeParticipantResponse> challengeParticipantResponses = new ArrayList<>();
        for (UserChallenge userChallenge : userChallenges) {
            Challenge challenge = userChallenge.getChallenge();
            List<Category> categories = challenge.getChallengeCategories().stream().map(ChallengeCategory::getCategory).toList();

            long totalSpend = accountBookRepository.sumSpendingInCategories(user.getId(), CategoryType.SPEND, categories, challenge.getStartDate(), challenge.getEndDate());
            Integer amount = challenge.getAmount();

            LocalDate currentDate = LocalDate.now();
            LocalDate endDate = challenge.getEndDate();
            int endDay = (int) ChronoUnit.DAYS.between(currentDate, endDate);
            log.info("두날짜의 차이 : {}", endDay);
            boolean isSuccess = userChallenge.isSuccess();
            boolean isWriteTip = userChallenge.isWriteTip();

            // 소비만 모으면됨
            ChallengeParticipantResponse response = new ChallengeParticipantResponse(
                    challenge.getId(),
                    challenge.getTitle(),
                    totalSpend,
                    amount,
                    endDay,
                    isSuccess,
                    isWriteTip
            );
            challengeParticipantResponses.add(response);
        }
        return challengeParticipantResponses;
    }

    private void checkExist(UserEntity user, Long id) {
        boolean isExist = userChallengeRepository.existsByUserIdAndChallengeId(user.getId(), id);

        if (isExist) {
            throw new ChallengeException(CHALLENGE_ALREADY_JOINED);
        }
    }

    private void checkNotExist(UserEntity user, Long challengeId) {
        boolean isNotExist = !userChallengeRepository.existsByUserIdAndChallengeId(user.getId(), challengeId);

        if (isNotExist) {
            throw new ChallengeException(CHALLENGE_NOT_FOUND_USER);
        }
    }

    private UserEntity getUser(UserEntity user) {
        return userRepository.findById(user.getId()).orElseThrow(() -> new UserErrorException(USER_NOT_FOUND));
    }

    private Challenge getMyChallenge(Long userId, Long id) {
        return challengeRepository.findByIdAndCreatorId(id, userId).orElseThrow(() -> new ChallengeException(CHALLENGE_INVALID));
    }
}
