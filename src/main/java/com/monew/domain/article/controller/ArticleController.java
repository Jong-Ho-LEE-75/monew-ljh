package com.monew.domain.article.controller;

import com.monew.common.dto.CursorRequest;
import com.monew.common.dto.PageResponse;
import com.monew.domain.article.dto.ArticleDto;
import com.monew.domain.article.dto.ArticleSearchCondition;
import com.monew.domain.article.dto.ArticleSortBy;
import com.monew.domain.article.dto.SortDirection;
import com.monew.domain.article.service.ArticleService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

    private static final String USER_HEADER = "MoNew-Request-User-ID";

    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<PageResponse<ArticleDto>> findAll(
        @RequestParam(required = false) UUID interestId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) List<String> sourceIn,
        @RequestParam(required = false) Instant publishedFrom,
        @RequestParam(required = false) Instant publishedTo,
        @RequestParam(required = false) ArticleSortBy sortBy,
        @RequestParam(required = false) SortDirection direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Integer size,
        @RequestHeader(value = USER_HEADER, required = false) UUID userId
    ) {
        ArticleSearchCondition condition = new ArticleSearchCondition(
            keyword, sourceIn, publishedFrom, publishedTo, interestId, sortBy, direction
        );
        return ResponseEntity.ok(
            articleService.findAll(condition, new CursorRequest(cursor, size), userId)
        );
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDto> getArticle(
        @PathVariable UUID articleId,
        @RequestHeader(USER_HEADER) UUID userId
    ) {
        return ResponseEntity.ok(articleService.findById(articleId, userId));
    }

    @GetMapping("/sources")
    public ResponseEntity<List<String>> getSources() {
        return ResponseEntity.ok(articleService.getSources());
    }

    @PostMapping("/{articleId}/article-views")
    public ResponseEntity<ArticleDto> view(
        @PathVariable UUID articleId,
        @RequestHeader(USER_HEADER) UUID userId
    ) {
        return ResponseEntity.ok(articleService.view(articleId, userId));
    }

    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID articleId) {
        articleService.softDelete(articleId);
        return ResponseEntity.noContent().build();
    }
}
