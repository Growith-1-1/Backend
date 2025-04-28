package dev.book.accountbook.controller;

import dev.book.accountbook.controller.swagger.AccountBookApi;
import dev.book.accountbook.dto.request.AccountBookIncomeRequest;
import dev.book.accountbook.dto.request.AccountBookMonthRequest;
import dev.book.accountbook.dto.request.AccountBookSpendListRequest;
import dev.book.accountbook.dto.request.AccountBookSpendRequest;
import dev.book.accountbook.dto.response.*;
import dev.book.accountbook.service.AccountBookService;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.user.entity.UserEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/account-book")
public class AccountBookController implements AccountBookApi {
    private final AccountBookService accountBookService;

    @Override
    @GetMapping("/spend")
    public ResponseEntity<AccountBookSpendListResponse> getSpendList(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam int page) {
        Long userId = userDetails.user().getId();
        AccountBookSpendListResponse responses = accountBookService.getSpendList(userId, page);

        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/spend/detail/{id}")
    public ResponseEntity<AccountBookSpendResponse> getSpendOne(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        Long userId = userDetails.user().getId();
        AccountBookSpendResponse response = accountBookService.getSpendOne(id, userId);

        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/spend")
    public ResponseEntity<AccountBookSpendResponse> createSpend(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookSpendRequest request) {
        UserEntity userId = userDetails.user();
        AccountBookSpendResponse response = accountBookService.createSpend(request, userId);

        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/spend/{id}")
    public ResponseEntity<AccountBookSpendResponse> modifySpend(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookSpendRequest request, @PathVariable Long id) {
        Long userId = userDetails.user().getId();
        AccountBookSpendResponse response = accountBookService.modifySpend(request, id, userId);

        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/spend/{id}")
    public ResponseEntity<Boolean> deleteSpend(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        Long userId = userDetails.user().getId();
        boolean response = accountBookService.deleteSpend(id, userId);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/income")
    public ResponseEntity<AccountBookIncomeListResponse> getIncomeList(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam int page) {
        Long userId = userDetails.user().getId();
        AccountBookIncomeListResponse responses = accountBookService.getIncomeList(userId, page);

        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/income/detail/{id}")
    public ResponseEntity<AccountBookIncomeResponse> getIncomeOne(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        Long userId = userDetails.user().getId();
        AccountBookIncomeResponse response = accountBookService.getIncomeOne(id, userId);

        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/income")
    public ResponseEntity<AccountBookIncomeResponse> createIncome(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookIncomeRequest request) {
        UserEntity user = userDetails.user();
        AccountBookIncomeResponse response = accountBookService.createIncome(request, user);

        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/income/{id}")
    public ResponseEntity<AccountBookIncomeResponse> modifyIncome(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookIncomeRequest request, @PathVariable Long id) {
        Long userId = userDetails.user().getId();
        AccountBookIncomeResponse response = accountBookService.modifyIncome(id, request, userId);

        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/income/{id}")
    public ResponseEntity<Boolean> deleteIncome(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        Long userId = userDetails.user().getId();
        boolean response = accountBookService.deleteIncome(id, userId);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/spend/category")
    public ResponseEntity<AccountBookSpendListResponse> getCategorySpendList(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam String category, @RequestParam int page) {
        Long userId = userDetails.user().getId();
        AccountBookSpendListResponse responses = accountBookService.getCategorySpendList(category, userId, page);

        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/all")
    public ResponseEntity<AccountBookPeriodListResponse> getAccountBookPeriod(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate, @RequestParam int page) {
        Long userId = userDetails.user().getId();
        AccountBookPeriodListResponse responseList = accountBookService.getAccountBookPeriod(userId, startDate, endDate, page);

        return ResponseEntity.ok(responseList);
    }

    @Override
    @PostMapping("/month")
    public ResponseEntity<List<AccountBookMonthResponse>> getMonthAccountBook(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody AccountBookMonthRequest request) {
        Long userId = userDetails.user().getId();
        List<AccountBookMonthResponse> responseList = accountBookService.getMonthAccountBook(userId, request);

        return ResponseEntity.ok(responseList);
    }

    @Override
    @PostMapping("/spend-list")
    public ResponseEntity<List<AccountBookSpendResponse>> createSpendList(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody AccountBookSpendListRequest requestList) {
        List<AccountBookSpendResponse> spendList = accountBookService.createSpendList(userDetails.user(), requestList);

        return ResponseEntity.ok(spendList);
    }

    @Override
    @GetMapping("/temp-list")
    public ResponseEntity<List<TempAccountBookResponse>> getTempAccountBook(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.user().getId();
        List<TempAccountBookResponse> tempList = accountBookService.getTempList(userId);

        return ResponseEntity.ok(tempList);
    }
}
