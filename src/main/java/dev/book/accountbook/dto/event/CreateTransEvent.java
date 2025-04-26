package dev.book.accountbook.dto.event;

import dev.book.user.entity.UserEntity;

public record CreateTransEvent(UserEntity user) {
}
