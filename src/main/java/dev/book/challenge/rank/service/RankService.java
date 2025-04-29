package dev.book.challenge.rank.service;

import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.challenge.ChallengeCategory;
import dev.book.challenge.entity.Challenge;
import dev.book.challenge.exception.ChallengeException;
import dev.book.challenge.exception.ErrorCode;
import dev.book.challenge.rank.dto.response.RankResponse;
import dev.book.challenge.repository.ChallengeRepository;
import dev.book.challenge.user_challenge.repository.UserChallengeRepository;
import dev.book.global.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankService {

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final AccountBookRepository accountBookRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void checkRank(Long challengeId) {
        // 카테고리를 조인해서 챌린지아이디로 조회 하고 그 챌린지의 속한 유저 아이디를 불러온다.
        Challenge challenge = challengeRepository.findByIdJoinCategory(challengeId).orElseThrow(() -> new ChallengeException(ErrorCode.CHALLENGE_NOT_FOUND));
        List<Long> participantIds = userChallengeRepository.findUserIdByChallengeId(challengeId);
        List<ChallengeCategory> challengeCategories = challenge.getChallengeCategories();
        List<Category> categories = challengeCategories.stream().map(ChallengeCategory::getCategory).toList();

        List<RankResponse> rankResponses = accountBookRepository.findByUserSpendingRanks(participantIds, categories, challenge.getStartDate(), challenge.getEndDate());
        simpMessagingTemplate.convertAndSend("/sub/challenge/" + challengeId + "/rank", rankResponses);


    }
}