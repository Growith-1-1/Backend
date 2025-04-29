package dev.book.challenge.controller;

import dev.book.challenge.controller.swagger.ChallengeApi;
import dev.book.challenge.dto.request.ChallengeCreateRequest;
import dev.book.challenge.dto.request.ChallengeUpdateRequest;
import dev.book.challenge.dto.response.*;
import dev.book.challenge.service.ChallengeLockService;
import dev.book.challenge.service.ChallengeService;
import dev.book.global.config.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeController implements ChallengeApi {

    private final ChallengeService challengeService;
    private final ChallengeLockService challengeLockService;

    @PostMapping
    public ResponseEntity<ChallengeCreateResponse> createChallenge(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody ChallengeCreateRequest challengeCreateRequest) {
        ChallengeCreateResponse challengeCreateResponse = challengeService.createChallenge(userDetails.user(), challengeCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(challengeCreateResponse);
    }

    @GetMapping
    public ResponseEntity<Page<ChallengeReadResponse>> searchChallenge(@RequestParam(required = false) String title,
                                                                       @RequestParam(required = false) String text,
                                                                       @RequestParam(required = false, defaultValue = "1") int page,
                                                                       @RequestParam(required = false, defaultValue = "10") int size) {
        Page<ChallengeReadResponse> challengeReadResponses = challengeService.searchChallenge(title, text, page, size);
        return ResponseEntity.ok().body(challengeReadResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChallengeReadDetailResponse> searchChallengeById(@PathVariable Long id) {
        ChallengeReadDetailResponse challengeReadResponse = challengeService.searchChallengeById(id);
        return ResponseEntity.ok().body(challengeReadResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChallengeUpdateResponse> updateChallenge(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, @Valid @RequestBody ChallengeUpdateRequest challengeUpdateRequest) {
        ChallengeUpdateResponse challengeUpdateResponse = challengeService.updateChallenge(userDetails.user(), id, challengeUpdateRequest);
        return ResponseEntity.ok().body(challengeUpdateResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChallenge(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        challengeService.deleteChallenge(userDetails.user(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/participation")
    public ResponseEntity<String> participate(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        challengeLockService.participate(userDetails.user(), id);
        return ResponseEntity.ok().body("참여가 완료 되었습니다");

    }

    @DeleteMapping("/{id}/exit")
    public ResponseEntity<String> leaveChallenge(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        challengeService.leaveChallenge(userDetails.user(), id);
        return ResponseEntity.ok().body("챌린지에서 나갔습니다");
    }

    @GetMapping("/top")
    public ResponseEntity<List<ChallengeTopResponse>> searchTopChallenge() {
        List<ChallengeTopResponse> topChallenge = challengeService.findTopChallenge();
        return ResponseEntity.ok().body(topChallenge);
    }

    @GetMapping("/new")
    public ResponseEntity<List<ChallengeReadResponse>> searchNewChallenge(
            @RequestParam(required = false, defaultValue = "1") int page
            , @RequestParam(required = false, defaultValue = "10") int size) {
        List<ChallengeReadResponse> challengeReadResponses = challengeService.findNewChallenge(page, size);
        return ResponseEntity.ok().body(challengeReadResponses);
    }

    @GetMapping("/me")
    public ResponseEntity<List<ChallengeParticipantResponse>> searchMyChallenge(
            @AuthenticationPrincipal CustomUserDetails userDetails
            , @RequestParam(required = false, defaultValue = "1") int page
            , @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.ok().body(challengeService.findMyChallenge(userDetails.user(), page, size));
    }
}
