package com.monew.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    DUPLICATE_USER(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다"),

    // Interest
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "관심사를 찾을 수 없습니다"),
    DUPLICATE_INTEREST_NAME(HttpStatus.CONFLICT, "80% 이상 유사한 이름의 관심사가 이미 존재합니다"),
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "구독 정보를 찾을 수 없습니다"),

    // Article
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "뉴스 기사를 찾을 수 없습니다"),
    DUPLICATE_ARTICLE_SOURCE_LINK(HttpStatus.CONFLICT, "이미 등록된 원본 기사 링크입니다"),

    // Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다"),
    COMMENT_NOT_OWNED(HttpStatus.FORBIDDEN, "본인의 댓글만 수정/삭제할 수 있습니다"),

    // Activity
    ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "활동 내역을 찾을 수 없습니다"),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다"),

    // Auth / Common
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다");

    private final HttpStatus status;
    private final String message;
}
