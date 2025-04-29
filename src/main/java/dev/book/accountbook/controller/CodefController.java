package dev.book.accountbook.controller;

import dev.book.accountbook.controller.swagger.CodefApi;
import dev.book.accountbook.dto.request.CreateConnectedIdRequest;
import dev.book.accountbook.dto.response.TempAccountBookResponse;
import dev.book.accountbook.service.CodefService;
import dev.book.global.config.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/codef")
@RequiredArgsConstructor
public class CodefController implements CodefApi {
    private final CodefService codefService;

    @Override
    @Profile("local")
    @GetMapping("/token")
    public void token() {
        codefService.getAccessToken();
    }

    @Override
    @PostMapping("/connect")
    public ResponseEntity<Boolean> connect(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody CreateConnectedIdRequest request) {
        boolean success = codefService.createConnectedId(userDetails.user(), request);

        return ResponseEntity.ok(success);
    }

    @Override
    @Profile("local")
    @GetMapping("/trans")
    public ResponseEntity<List<TempAccountBookResponse>> trans(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<TempAccountBookResponse> decodeList = codefService.getTransactions(userDetails.user());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(decodeList);
    }
}
