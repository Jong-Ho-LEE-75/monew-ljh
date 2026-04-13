package com.monew.domain.comment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.monew.common.dto.CursorRequest;
import com.monew.common.dto.PageResponse;
import com.monew.domain.comment.dto.CommentDto;
import com.monew.domain.comment.dto.CommentSortBy;
import com.monew.domain.comment.dto.request.CommentCreateRequest;
import com.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.monew.domain.comment.service.CommentService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController controller;

    private CommentDto sampleDto() {
        return new CommentDto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            "n", "내용", 0L, false, Instant.now(), Instant.now());
    }

    @Test
    void create_위임() {
        UUID userId = UUID.randomUUID();
        CommentCreateRequest req = new CommentCreateRequest(UUID.randomUUID(), "내용");
        CommentDto dto = sampleDto();
        given(commentService.create(userId, req)).willReturn(dto);

        ResponseEntity<CommentDto> response = controller.create(userId, req);

        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void findByArticle_커서_위임() {
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PageResponse<CommentDto> page = new PageResponse<>(List.of(sampleDto()), null, 1, false, null);
        given(commentService.findByArticle(eq(articleId), eq(userId), eq(CommentSortBy.LIKE_COUNT), any(CursorRequest.class)))
            .willReturn(page);

        ResponseEntity<PageResponse<CommentDto>> response = controller.findByArticle(
            articleId, userId, CommentSortBy.LIKE_COUNT, null, 10);

        assertThat(response.getBody()).isEqualTo(page);
    }

    @Test
    void update_위임() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        CommentUpdateRequest req = new CommentUpdateRequest("수정");
        CommentDto dto = sampleDto();
        given(commentService.update(userId, commentId, req)).willReturn(dto);

        ResponseEntity<CommentDto> response = controller.update(userId, commentId, req);

        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void softDelete_204() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.softDelete(userId, commentId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(commentService).should(times(1)).softDelete(userId, commentId);
    }

    @Test
    void like_위임() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        CommentDto dto = sampleDto();
        given(commentService.like(userId, commentId)).willReturn(dto);

        ResponseEntity<CommentDto> response = controller.like(userId, commentId);

        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void unlike_위임() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        CommentDto dto = sampleDto();
        given(commentService.unlike(userId, commentId)).willReturn(dto);

        ResponseEntity<CommentDto> response = controller.unlike(userId, commentId);

        assertThat(response.getBody()).isEqualTo(dto);
    }
}
