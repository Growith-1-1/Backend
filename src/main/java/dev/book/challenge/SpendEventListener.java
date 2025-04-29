package dev.book.challenge;

import dev.book.accountbook.entity.AccountBook;
import dev.book.challenge.entity.Challenge;
import dev.book.challenge.rank.SpendCreatedRankingEvent;
import dev.book.challenge.rank.service.RankService;
import dev.book.challenge.user_challenge.repository.UserChallengeRepository;
import dev.book.global.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Component
public class SpendEventListener {

    private final UserChallengeRepository userChallengeRepository;
    private final RankService rankService;
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSpendCreatedEvent(SpendCreatedRankingEvent event) {
        AccountBook accountBook = event.accountBook();
        Long userId = accountBook.getUser().getId();
        LocalDate spendDate = accountBook.getOccurredAt();
        Category spendCategory = accountBook.getCategory();
        List<Challenge> joinedChallenges = userChallengeRepository.findChallengesByUserAndDate(userId, spendCategory.getId(), spendDate);
        // 참여자 전체 순위 재계산 후 전송
        for (Challenge challenge : joinedChallenges) {
            rankService.checkRank(challenge.getId());
        }
    }

}
