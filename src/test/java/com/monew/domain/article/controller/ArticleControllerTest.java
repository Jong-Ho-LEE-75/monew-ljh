package com.monew.domain.article.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ArticleControllerTest {

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private ArticleController controller;

    private ArticleDto sampleDto() {
        return new ArticleDto(UUID.randomUUID(), "NAVER", "https://x", "title", "summary",
            Instant.now(), null, null, 0L, 0L, false);
    }

    @Test
    void findAll_검색조건_조립_위임() {
        UUID userId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        PageResponse<ArticleDto> page = new PageResponse<>(List.of(sampleDto()), null, 1, false);
        given(articleService.findAll(any(ArticleSearchCondition.class), any(CursorRequest.class), eq(userId)))
            .willReturn(page);

        Instant from = Instant.parse("2026-04-01T00:00:00Z");
        Instant to = Instant.parse("2026-04-10T00:00:00Z");
        ResponseEntity<PageResponse<ArticleDto>> response = controller.findAll(
            interestId, "kw", List.of("NAVER"), from, to,
            ArticleSortBy.VIEW_COUNT, SortDirection.DESC, null, 10, userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page);
    }

    @Test
    void view_위임() {
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ArticleDto dto = sampleDto();
        given(articleService.view(articleId, userId)).willReturn(dto);

        ResponseEntity<ArticleDto> response = controller.view(articleId, userId);

        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void softDelete_204() {
        UUID articleId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.softDelete(articleId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(articleService).should(times(1)).softDelete(articleId);
    }
}
