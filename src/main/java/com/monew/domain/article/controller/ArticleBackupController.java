package com.monew.domain.article.controller;

import com.monew.common.exception.ErrorCode;
import com.monew.common.exception.MonewException;
import com.monew.domain.article.backup.ArticleBackupService;
import com.monew.domain.article.dto.ArticleRestoreResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles/backups")
public class ArticleBackupController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ArticleBackupService articleBackupService;

    @Value("${monew.backup.admin-token:}")
    private String adminToken;

    @GetMapping
    public ResponseEntity<List<LocalDate>> listBackupDates() {
        return ResponseEntity.ok(articleBackupService.listBackupDates());
    }

    @PostMapping("/restore")
    public ResponseEntity<ArticleRestoreResponse> restore(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        authorize(authorization);
        int restored = articleBackupService.restore(date);
        return ResponseEntity.ok(new ArticleRestoreResponse(date, restored));
    }

    private void authorize(String authorization) {
        if (adminToken == null || adminToken.isBlank()) {
            throw new MonewException(ErrorCode.FORBIDDEN,
                java.util.Map.of("reason", "admin-token-not-configured"));
        }
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new MonewException(ErrorCode.UNAUTHENTICATED,
                java.util.Map.of("reason", "missing-authorization"));
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (!adminToken.equals(token)) {
            throw new MonewException(ErrorCode.FORBIDDEN,
                java.util.Map.of("reason", "invalid-admin-token"));
        }
    }
}
