package dev.book.challenge;

import dev.book.challenge.dto.request.ChallengeCreateRequest;
import dev.book.challenge.entity.Challenge;
import dev.book.challenge.repository.ChallengeRepository;
import dev.book.challenge.service.ChallengeLockService;
import dev.book.challenge.service.ChallengeService;
import dev.book.challenge.user_challenge.repository.UserChallengeRepository;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.global.config.security.jwt.JwtAuthenticationToken;
import dev.book.user.entity.UserEntity;
import dev.book.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class ChallengeConcurrencyTest {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private UserChallengeRepository userChallengeRepository;

    @Autowired
    private ChallengeLockService challengeLockService;

    @AfterEach
    void tearDown() {
        userChallengeRepository.deleteAllInBatch();
        challengeRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("동시에 참여했을 때 동시성 테스트.")
    @WithMockUser
    void ChallengeConcurrency() throws InterruptedException {

        //given
        int numThreads = 10; //10명
        CountDownLatch countDownLatch = new CountDownLatch(numThreads);
        CountDownLatch startCountDownLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        List<UserEntity> users = new ArrayList<>();

        for (int i = 1; i <= numThreads; i++) {
            UserEntity user = UserEntity.builder()
                    .email("test@naver.com" + i)
                    .name("사용자" + i)
                    .nickname("user" + i)
                    .build();
            users.add(user);
        }

        userRepository.saveAll(users);

        UserEntity creator = UserEntity.builder().email("test@naver.com").name("생성자").nickname("naver").build();
        UserEntity savedCreator = userRepository.save(creator);
        LocalDate startDate = LocalDate.of(2025, 04, 24);
        LocalDate endDate = LocalDate.of(2025, 04, 30);

        ChallengeCreateRequest challengeCreateRequest = new ChallengeCreateRequest("제목", "내용", "PUBLIC", 100000, 11, List.of("SHOPPING"), startDate, endDate);
        Challenge challenge = Challenge.of(challengeCreateRequest, savedCreator);
        Challenge savedChallenge = challengeRepository.save(challenge);

        for (int i = 1; i <= numThreads; i++) {

            final int index = i;
            executorService.execute(() -> {
                try {
                    UserEntity userEntity = userRepository.findByEmail("test@naver.com" + index).orElseThrow();
                    SecurityContext context = SecurityContextHolder.createEmptyContext();

                    CustomUserDetails userDetails = new CustomUserDetails(userEntity);
                    JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails, userDetails.getAuthorities());

                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                    startCountDownLatch.await(); // 동시에 출발할려고 기다림
                    challengeLockService.participate(userEntity, challenge.getId());
                } catch (Exception e) {
                    log.error(e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        startCountDownLatch.countDown(); // 1->0으로 변하고 participate을 거의 동시에 호출
        countDownLatch.await();
        //when
        Challenge updatedChallenge = challengeRepository.findById(savedChallenge.getId()).orElseThrow();
        //then
        assertThat(updatedChallenge.getCurrentCapacity()).isEqualTo(11);
    }
}
