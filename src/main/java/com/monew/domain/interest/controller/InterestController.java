package com.monew.domain.interest.controller;

import com.monew.common.dto.CursorRequest;
import com.monew.common.dto.PageResponse;
import com.monew.domain.article.dto.SortDirection;
import com.monew.domain.interest.dto.InterestDto;
import com.monew.domain.interest.dto.InterestSearchCondition;
import com.monew.domain.interest.dto.InterestSortBy;
import com.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.monew.domain.interest.dto.request.InterestUpdateRequest;
import com.monew.domain.interest.service.InterestService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/interests")
public class InterestController {

    private static final String USER_HEADER = "MoNew-Request-User-ID";

    private final InterestService interestService;

    @PostMapping
    public ResponseEntity<InterestDto> register(@Valid @RequestBody InterestRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interestService.register(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<InterestDto>> findAll(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) InterestSortBy sortBy,
        @RequestParam(required = false) SortDirection direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Integer size,
        @RequestHeader(value = USER_HEADER, required = false) UUID userId
    ) {
        InterestSearchCondition condition = new InterestSearchCondition(keyword, sortBy, direction);
        return ResponseEntity.ok(
            interestService.findAll(condition, new CursorRequest(cursor, size), userId)
        );
    }

    @PatchMapping("/{interestId}")
    public ResponseEntity<InterestDto> updateKeywords(
        @PathVariable UUID interestId,
        @Valid @RequestBody InterestUpdateRequest request
    ) {
        return ResponseEntity.ok(interestService.updateKeywords(interestId, request));
    }

    @DeleteMapping("/{interestId}")
    public ResponseEntity<Void> delete(@PathVariable UUID interestId) {
        interestService.delete(interestId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{interestId}/subscriptions")
    public ResponseEntity<InterestDto> subscribe(
        @PathVariable UUID interestId,
        @RequestHeader(USER_HEADER) UUID userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(interestService.subscribe(userId, interestId));
    }

    @DeleteMapping("/{interestId}/subscriptions")
    public ResponseEntity<Void> unsubscribe(
        @PathVariable UUID interestId,
        @RequestHeader(USER_HEADER) UUID userId
    ) {
        interestService.unsubscribe(userId, interestId);
        return ResponseEntity.noContent().build();
    }
}
