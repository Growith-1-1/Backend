package dev.book.challenge.service;

import dev.book.achievement.achievement_user.IndividualAchievementStatusService;
import dev.book.challenge.ChallengeCategory;
import dev.book.challenge.category.ChallengeCategoryRepository;
import dev.book.challenge.dto.request.ChallengeCreateRequest;
import dev.book.challenge.dto.request.ChallengeUpdateRequest;
import dev.book.challenge.dto.response.ChallengeCreateResponse;
import dev.book.challenge.dto.response.ChallengeReadDetailResponse;
import dev.book.challenge.dto.response.ChallengeReadResponse;
import dev.book.challenge.dto.response.ChallengeUpdateResponse;
import dev.book.challenge.entity.Challenge;
import dev.book.challenge.exception.ChallengeException;
import dev.book.challenge.exception.ErrorCode;
import dev.book.challenge.repository.ChallengeRepository;
import dev.book.challenge.user_challenge.entity.UserChallenge;
import dev.book.challenge.user_challenge.repository.UserChallengeRepository;
import dev.book.global.entity.Category;
import dev.book.global.repository.CategoryRepository;
import dev.book.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.book.challenge.exception.ErrorCode.CHALLENGE_ALREADY_JOINED;
import static dev.book.challenge.exception.ErrorCode.CHALLENGE_NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final IndividualAchievementStatusService individualAchievementStatusService;
    private final CategoryRepository categoryRepository;


    public ChallengeCreateResponse createChallenge(UserEntity user, ChallengeCreateRequest challengeCreateRequest) {
        List<Category> categories = categoryRepository.findByCategoryIn(challengeCreateRequest.categoryList());
        Challenge challenge = Challenge.of(challengeCreateRequest, user);
        categories.forEach(category -> new ChallengeCategory(challenge, category));
        Challenge savedChallenge = challengeRepository.save(challenge);
        UserChallenge userChallenge = UserChallenge.of(user, savedChallenge);
        userChallengeRepository.save(userChallenge);
        individualAchievementStatusService.plusCreateChallenge(user);
        return ChallengeCreateResponse.fromEntity(challenge);

    }

    public Page<ChallengeReadResponse> searchChallenge(String title, String text, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return challengeRepository.search(title, text, pageable);
    }

    public ChallengeReadDetailResponse searchChallengeById(Long id) {
        Challenge challenge = challengeRepository.findWithCreatorById(id).orElseThrow(() -> new ChallengeException(ErrorCode.CHALLENGE_NOT_FOUND));
        return ChallengeReadDetailResponse.fromEntity(challenge);
    }

    @Transactional
    public ChallengeUpdateResponse updateChallenge(UserEntity user, Long id, ChallengeUpdateRequest challengeUpdateRequest) {
        Challenge challenge = getMyChallenge(user.getId(), id);

        List<Category> categories = categoryRepository.findByCategoryIn(challengeUpdateRequest.categoryList());
        challenge.updateInfo(challengeUpdateRequest, categories);
        challengeRepository.flush();
        return ChallengeUpdateResponse.fromEntity(challenge, categories);
    }

    @Transactional
    public void deleteChallenge(UserEntity user, Long id) {
        Challenge challenge = getMyChallenge(user.getId(), id);
        challengeRepository.delete(challenge);

    }

    @Transactional
    public void participate(UserEntity user, Long id) {

        Challenge challenge = challengeRepository.findById(id).orElseThrow(() -> new ChallengeException(ErrorCode.CHALLENGE_NOT_FOUND));
        challenge.checkAlreadyStartOrEnd();
        checkExist(user, id);

        long countParticipants = userChallengeRepository.countByChallengeId(id);
        challenge.isOver(countParticipants);
        UserChallenge userChallenge = UserChallenge.of(user, challenge);
        userChallengeRepository.save(userChallenge);
    }


    @Transactional
    public void leaveChallenge(UserEntity user, Long challengeId) {

        checkNotExist(user, challengeId);
        userChallengeRepository.deleteByUserIdAndChallengeId(user.getId(), challengeId);

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

    private Challenge getMyChallenge(Long userId, Long id) {
        Challenge challenge = challengeRepository.findByIdAndCreatorId(id, userId).orElseThrow(() -> new ChallengeException(ErrorCode.CHALLENGE_INVALID));
        return challenge;
    }
}
