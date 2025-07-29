package dev.book.global.config.firebase.dto;

public record LimitWarningFcmEvent (Long userId, String nickname, int budget, long total, long usageRate){
}
