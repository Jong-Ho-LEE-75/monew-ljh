package com.monew.domain.notification.controller;

import com.monew.common.dto.CursorRequest;
import com.monew.common.dto.PageResponse;
import com.monew.domain.notification.dto.NotificationDto;
import com.monew.domain.notification.service.NotificationService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final String USER_HEADER = "MoNew-Request-User-ID";

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<PageResponse<NotificationDto>> findUnconfirmed(
        @RequestHeader(USER_HEADER) UUID userId,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(
            notificationService.findUnconfirmed(userId, new CursorRequest(cursor, size)));
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<Void> confirm(
        @RequestHeader(USER_HEADER) UUID userId,
        @PathVariable UUID notificationId
    ) {
        notificationService.confirm(userId, notificationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    public ResponseEntity<Map<String, Integer>> confirmAll(
        @RequestHeader(USER_HEADER) UUID userId
    ) {
        int updated = notificationService.confirmAll(userId);
        return ResponseEntity.ok(Map.of("updated", updated));
    }
}
