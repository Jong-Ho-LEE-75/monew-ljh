package com.monew.domain.comment.controller;

import com.monew.common.dto.CursorRequest;
import com.monew.common.dto.PageResponse;
import com.monew.domain.article.dto.SortDirection;
import com.monew.domain.comment.dto.CommentDto;
import com.monew.domain.comment.dto.CommentSortBy;
import com.monew.domain.comment.dto.request.CommentCreateRequest;
import com.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.monew.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private static final String USER_HEADER = "MoNew-Request-User-ID";

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> create(
        @RequestHeader(USER_HEADER) UUID userId,
        @RequestBody @Valid CommentCreateRequest request
    ) {
        return ResponseEntity.ok(commentService.create(userId, request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CommentDto>> findByArticle(
        @RequestParam UUID articleId,
        @RequestHeader(value = USER_HEADER, required = false) UUID userId,
        @RequestParam(required = false, name = "orderBy") CommentSortBy sortBy,
        @RequestParam(required = false) SortDirection direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false, name = "limit") Integer size
    ) {
        return ResponseEntity.ok(
            commentService.findByArticle(articleId, userId, sortBy, direction, new CursorRequest(cursor, size)));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> update(
        @RequestHeader(USER_HEADER) UUID userId,
        @PathVariable UUID commentId,
        @RequestBody @Valid CommentUpdateRequest request
    ) {
        return ResponseEntity.ok(commentService.update(userId, commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> softDelete(
        @RequestHeader(USER_HEADER) UUID userId,
        @PathVariable UUID commentId
    ) {
        commentService.softDelete(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/likes")
    public ResponseEntity<CommentDto> like(
        @RequestHeader(USER_HEADER) UUID userId,
        @PathVariable UUID commentId
    ) {
        return ResponseEntity.ok(commentService.like(userId, commentId));
    }

    @DeleteMapping("/{commentId}/likes")
    public ResponseEntity<CommentDto> unlike(
        @RequestHeader(USER_HEADER) UUID userId,
        @PathVariable UUID commentId
    ) {
        return ResponseEntity.ok(commentService.unlike(userId, commentId));
    }
}
