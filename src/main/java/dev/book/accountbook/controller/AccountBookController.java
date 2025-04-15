package dev.book.accountbook.controller;

import dev.book.accountbook.dto.request.AccountBookIncomeRequest;
import dev.book.accountbook.dto.request.AccountBookSpendRequest;
import dev.book.accountbook.dto.response.AccountBookIncomeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendResponse;
import dev.book.accountbook.service.AccountBookService;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.user.entity.UserEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account-book")
@RequiredArgsConstructor
public class AccountBookController {
    private final AccountBookService accountBookService;

    @GetMapping("/spend")
    public ResponseEntity<List<AccountBookSpendResponse>> spendList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        List<AccountBookSpendResponse> responses = accountBookService.getSpendList(userId);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/spend/{id}")
    public ResponseEntity<AccountBookSpendResponse> getSpendOne(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        Long userId = userDetails.getUser().getId();
        AccountBookSpendResponse response = accountBookService.getSpendOne(id,userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/spend")
    public ResponseEntity<AccountBookSpendResponse> createSpend(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookSpendRequest request) {
        UserEntity userId = userDetails.getUser();
        AccountBookSpendResponse response = accountBookService.createSpend(request, userId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/spend/{id}")
    public ResponseEntity<AccountBookSpendResponse> modifySpend(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookSpendRequest request, @PathVariable Long id) {
        Long userId = userDetails.getUser().getId();
        AccountBookSpendResponse response = accountBookService.modifySpend(request, id, userId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/spend/{id}")
    public ResponseEntity<Boolean> deleteSpend(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        Long userId = userDetails.getUser().getId();
        boolean response = accountBookService.deleteSpend(id, userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/income")
    public ResponseEntity<List<AccountBookIncomeResponse>> incomeList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        List<AccountBookIncomeResponse> responses = accountBookService.getIncomeList(userId);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/income/{id}")
    public ResponseEntity<AccountBookIncomeResponse> getIncomeOne(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        Long userId = userDetails.getUser().getId();
        AccountBookIncomeResponse response = accountBookService.getIncomeOne(id, userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/income")
    public ResponseEntity<AccountBookIncomeResponse> createIncome(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookIncomeRequest request) {
        UserEntity user = userDetails.getUser();
        AccountBookIncomeResponse response = accountBookService.createIncome(request, user);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/income/{id}")
    public ResponseEntity<AccountBookIncomeResponse> modifyIncome(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookIncomeRequest request, @PathVariable Long id) {
        Long userId = userDetails.getUser().getId();
        AccountBookIncomeResponse response = accountBookService.modifyIncome(id, request, userId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/income/{id}")
    public ResponseEntity<Boolean> deleteIncome(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        Long userId = userDetails.getUser().getId();
        boolean response = accountBookService.deleteIncome(id, userId);

        return ResponseEntity.ok(response);
    }
}
