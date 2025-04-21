package dev.book.accountbook.controller;

import dev.book.accountbook.controller.swagger.BudgetApi;
import dev.book.accountbook.dto.request.BudgetRequest;
import dev.book.accountbook.dto.response.BudgetResponse;
import dev.book.accountbook.service.BudgetService;
import dev.book.global.config.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/budget")
public class BudgetController implements BudgetApi {
    private final BudgetService budgetService;

    @Override
    @GetMapping
    public ResponseEntity<BudgetResponse> getBudgetList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.user().getId();
        BudgetResponse response = budgetService.getBudget(userId);

        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody BudgetRequest budgetRequest) {
        BudgetResponse response = budgetService.createBudget(userDetails.user(), budgetRequest);

        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> modifyBudget(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, @RequestBody BudgetRequest budgetRequest) {
        Long userId = userDetails.user().getId();
        BudgetResponse response = budgetService.modify(userId, id, budgetRequest);

        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        Long userId = userDetails.user().getId();
        budgetService.deleteBudget(userId, id);

        return null;
    }
}
