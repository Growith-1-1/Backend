package dev.book.challenge.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    CHALLENGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 챌린지를 찾을 수 없습니다."),
    CHALLENGE_INVALID(HttpStatus.FORBIDDEN, "수정 및 삭제 권한이 없습니다."),
    CHALLENGE_CAPACITY_FULL(HttpStatus.CONFLICT, "참여 인원이 초과 하였습니다."),
    CHALLENGE_ALREADY_JOINED(HttpStatus.CONFLICT, "이미 참여된 챌린지 입니다."),
    CHALLENGE_ALREADY_INVITED(HttpStatus.CONFLICT, "이미 초대된 요청입니다."),
    CHALLENGE_ALREADY_ACCEPT(HttpStatus.CONFLICT, "이미 수락한 요청입니다."),
    CHALLENGE_INVITE_INVALID(HttpStatus.CONFLICT, "초대할 권한이 없습니다."),
    CHALLENGE_NOT_FOUND_INVITED(HttpStatus.NOT_FOUND, "챌린지 초대의 유효한 접근이 아닙니다."),
    CHALLENGE_NOT_FOUND_USER(HttpStatus.NOT_FOUND, "챌린지에 속해있지 않은 사용자 입니다."),
    CHALLENGE_ALREADY_START(HttpStatus.BAD_REQUEST, "이미 시작한 챌린지 입니다."),
    CHALLENGE_ALREADY_END(HttpStatus.BAD_REQUEST, "이미 종료한 챌린지 입니다."),
    CHALLENGE_PARTICIPANT_FAIL(HttpStatus.BAD_REQUEST, "챌린지 참여에 실패 하였습니다.");

    private final HttpStatus httpStatus;

    private final String message;
}
