package dev.book.accountbook.controller;

import dev.book.accountbook.controller.swagger.StatApi;
import dev.book.accountbook.dto.response.AccountBookConsumeResponse;
import dev.book.accountbook.dto.response.AccountBookListResponse;
import dev.book.accountbook.dto.response.AccountBookStatResponse;
import dev.book.accountbook.service.StatService;
import dev.book.accountbook.type.Frequency;
import dev.book.global.config.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stat")
public class StatController implements StatApi {
    private final StatService statService;

    @Override
    @GetMapping("/{frequency}")
    public ResponseEntity<List<AccountBookStatResponse>> statList(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Frequency frequency) {
        Long userId = userDetails.user().getId();
        List<AccountBookStatResponse> list = statService.statList(userId, frequency);

        return ResponseEntity.ok(list);
    }

    @Override
    @GetMapping("/category/{frequency}")
    public ResponseEntity<AccountBookListResponse> categoryList(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Frequency frequency, @RequestParam String category, @RequestParam int page) {
        Long userId = userDetails.user().getId();
        AccountBookListResponse list = statService.categoryList(userId, frequency, category, page);

        return ResponseEntity.ok(list);
    }

    @Override
    @GetMapping("/consume/{frequency}")
    public ResponseEntity<AccountBookConsumeResponse> consume(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Frequency frequency) {
        Long userId = userDetails.user().getId();
        AccountBookConsumeResponse response = statService.consume(userId, frequency);

        return ResponseEntity.ok(response);
    }
}
