package com.monew.domain.article.controller;

import com.monew.domain.article.backup.ArticleBackupService;
import com.monew.domain.article.dto.ArticleRestoreResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles/backups")
public class ArticleBackupController {

    private final ArticleBackupService articleBackupService;

    @GetMapping
    public ResponseEntity<List<LocalDate>> listBackupDates() {
        return ResponseEntity.ok(articleBackupService.listBackupDates());
    }

    @PostMapping("/restore")
    public ResponseEntity<ArticleRestoreResponse> restore(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        int restored = articleBackupService.restore(date);
        return ResponseEntity.ok(new ArticleRestoreResponse(date, restored));
    }
}
