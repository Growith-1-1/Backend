package dev.book.tip.controller;

import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.tip.controller.swagger.TipApi;
import dev.book.tip.dto.request.TipRequest;
import dev.book.tip.service.TipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tips")
public class TipController implements TipApi {

    private final TipService tipService;

    @PostMapping
    public ResponseEntity<?> createTip(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody TipRequest tipRequest){
        tipService.createTip(userDetails, tipRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<?> getTips(){
        return ResponseEntity.ok().body(tipService.getTips());
    }
}
