package dev.book.global.config.Firebase;

import dev.book.accountbook.service.AccountBookService;
import dev.book.global.config.Firebase.service.FCMService;
import dev.book.global.config.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/fcm")
public class FcmController {
    private final FCMService fcmService;
    private final AccountBookService accountBookService;

    @PostMapping("/token")
    public void saveToken(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam String token) {
        fcmService.saveToken(userDetails.user(), token);
    }

    @GetMapping("/token")
    public ResponseEntity<String> sendFcmToken(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String token = fcmService.getToken(userDetails.user().getId());

        return ResponseEntity.ok(token);
    }

    @GetMapping("/send")
    public ResponseEntity<String> sendFcmMessage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String message = accountBookService.sendMessage(userDetails.user());

        return ResponseEntity.ok(message);
    }
}
