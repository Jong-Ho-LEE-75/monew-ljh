package com.monew.domain.article.service;

import com.monew.common.dto.CursorRequest;
import com.monew.common.dto.PageResponse;
import com.monew.domain.article.dto.ArticleDto;
import com.monew.domain.article.dto.ArticleSearchCondition;
import com.monew.domain.article.dto.SortDirection;
import com.monew.domain.article.entity.Article;
import com.monew.domain.article.entity.ArticleView;
import com.monew.domain.article.event.ArticleViewedEvent;
import com.monew.domain.article.exception.ArticleNotFoundException;
import com.monew.domain.article.mapper.ArticleMapper;
import com.monew.domain.article.repository.ArticleRepository;
import com.monew.domain.article.repository.ArticleSpecifications;
import com.monew.domain.article.repository.ArticleViewRepository;
import com.monew.domain.user.entity.User;
import com.monew.domain.user.exception.UserNotFoundException;
import com.monew.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleViewRepository articleViewRepository;
    private final UserRepository userRepository;
    private final ArticleMapper articleMapper;
    private final ApplicationEventPublisher eventPublisher;

    public PageResponse<ArticleDto> findAll(
        ArticleSearchCondition condition,
        CursorRequest cursorRequest,
        UUID currentUserId
    ) {
        int size = cursorRequest.sizeOrDefault();
        Instant cursor = parseCursor(cursorRequest.cursor());
        Specification<Article> spec = ArticleSpecifications.build(condition, cursor);
        Sort.Direction sortDirection = condition.directionOrDefault() == SortDirection.ASC
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(0, size + 1, Sort.by(sortDirection, "publishedAt"));
        List<Article> page = articleRepository.findAll(spec, pageable).getContent();
        List<ArticleDto> dtos = page.stream()
            .map(article -> articleMapper.toDto(article, isViewed(currentUserId, article.getId())))
            .toList();
        return PageResponse.of(dtos, size, dto -> dto.publishedAt().toString());
    }

    @Transactional
    public ArticleDto view(UUID articleId, UUID userId) {
        Article article = findActiveArticle(articleId);

        if (articleViewRepository.existsByArticle_IdAndUser_Id(articleId, userId)) {
            return articleMapper.toDto(article, true);
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        try {
            ArticleView view = articleViewRepository.saveAndFlush(ArticleView.of(article, user));
            article.incrementViewCount();
            eventPublisher.publishEvent(new ArticleViewedEvent(
                view.getId(),
                article.getId(),
                userId,
                article.getSource(),
                article.getSourceUrl(),
                article.getTitle(),
                article.getSummary(),
                article.getPublishedAt(),
                article.getViewCount(),
                0L,
                view.getCreatedAt() == null ? Instant.now() : view.getCreatedAt()
            ));
        } catch (DataIntegrityViolationException ignored) {
            // 경쟁 상태 fallback: 동시에 두 요청이 existsBy 통과 후 하나만 저장됨
        }

        return articleMapper.toDto(article, true);
    }

    @Transactional
    public void softDelete(UUID articleId) {
        Article article = findActiveArticle(articleId);
        article.softDelete();
    }

    private Article findActiveArticle(UUID articleId) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new ArticleNotFoundException(articleId));
        if (article.isDeleted()) {
            throw new ArticleNotFoundException(articleId);
        }
        return article;
    }

    private boolean isViewed(UUID userId, UUID articleId) {
        if (userId == null) {
            return false;
        }
        return articleViewRepository.existsByArticle_IdAndUser_Id(articleId, userId);
    }

    private Instant parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        return Instant.parse(cursor);
    }
}
