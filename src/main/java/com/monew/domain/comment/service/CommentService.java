package com.monew.domain.comment.service;

import com.monew.common.dto.CursorRequest;
import com.monew.common.dto.PageResponse;
import com.monew.domain.article.entity.Article;
import com.monew.domain.article.exception.ArticleNotFoundException;
import com.monew.domain.article.repository.ArticleRepository;
import com.monew.domain.comment.dto.CommentDto;
import com.monew.domain.comment.dto.CommentSortBy;
import com.monew.domain.comment.dto.request.CommentCreateRequest;
import com.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.monew.domain.comment.entity.Comment;
import com.monew.domain.comment.entity.CommentLike;
import com.monew.domain.comment.event.CommentCreatedEvent;
import com.monew.domain.comment.event.CommentLikedEvent;
import com.monew.domain.comment.event.CommentUnlikedEvent;
import com.monew.domain.comment.exception.CommentNotFoundException;
import com.monew.domain.comment.exception.CommentNotOwnedException;
import com.monew.domain.comment.mapper.CommentMapper;
import com.monew.domain.comment.repository.CommentLikeRepository;
import com.monew.domain.comment.repository.CommentRepository;
import com.monew.domain.user.entity.User;
import com.monew.domain.user.exception.UserNotFoundException;
import com.monew.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CommentDto create(UUID currentUserId, CommentCreateRequest request) {
        User user = getUser(currentUserId);
        Article article = articleRepository.findById(request.articleId())
            .orElseThrow(() -> new ArticleNotFoundException(request.articleId()));

        Comment comment = Comment.builder()
            .article(article)
            .user(user)
            .content(request.content())
            .build();
        Comment saved = commentRepository.save(comment);
        article.incrementCommentCount();
        eventPublisher.publishEvent(new CommentCreatedEvent(
            saved.getId(),
            article.getId(),
            article.getTitle(),
            user.getId(),
            user.getNickname(),
            saved.getContent(),
            saved.getCreatedAt() == null ? Instant.now() : saved.getCreatedAt()
        ));
        return commentMapper.toDto(saved, false);
    }

    @Transactional
    public CommentDto update(UUID currentUserId, UUID commentId, CommentUpdateRequest request) {
        Comment comment = getActiveComment(commentId);
        User user = getUser(currentUserId);
        if (!comment.isOwnedBy(user)) {
            throw new CommentNotOwnedException(commentId, currentUserId);
        }
        comment.updateContent(request.content());
        boolean likedByMe = commentLikeRepository.existsByComment_IdAndUser_Id(commentId, currentUserId);
        return commentMapper.toDto(comment, likedByMe);
    }

    @Transactional
    public void softDelete(UUID currentUserId, UUID commentId) {
        Comment comment = getActiveComment(commentId);
        User user = getUser(currentUserId);
        if (!comment.isOwnedBy(user)) {
            throw new CommentNotOwnedException(commentId, currentUserId);
        }
        comment.softDelete();
        comment.getArticle().decrementCommentCount();
    }

    public PageResponse<CommentDto> findByArticle(
        UUID articleId,
        UUID currentUserId,
        CommentSortBy sortBy,
        CursorRequest cursorRequest
    ) {
        int size = cursorRequest.sizeOrDefault();
        Pageable pageable = PageRequest.of(0, size + 1);
        CommentSortBy effectiveSort = sortBy == null ? CommentSortBy.CREATED_AT : sortBy;

        List<Comment> comments;
        if (effectiveSort == CommentSortBy.LIKE_COUNT) {
            Long cursor = parseLongCursor(cursorRequest.cursor());
            comments = cursor == null
                ? commentRepository.findFirstPageByArticleOrderByLikes(articleId, pageable)
                : commentRepository.findPageByArticleAfterLikes(articleId, cursor, pageable);
        } else {
            Instant cursor = parseCursor(cursorRequest.cursor());
            comments = cursor == null
                ? commentRepository.findFirstPageByArticle(articleId, pageable)
                : commentRepository.findPageByArticleAfter(articleId, cursor, pageable);
        }

        Set<UUID> likedIds = currentUserId == null
            ? Set.of()
            : comments.stream()
                .filter(c -> commentLikeRepository.existsByComment_IdAndUser_Id(c.getId(), currentUserId))
                .map(Comment::getId)
                .collect(Collectors.toSet());

        List<CommentDto> dtos = comments.stream()
            .map(c -> commentMapper.toDto(c, likedIds.contains(c.getId())))
            .toList();

        return PageResponse.of(dtos, size, dto ->
            effectiveSort == CommentSortBy.LIKE_COUNT
                ? Long.toString(dto.likeCount())
                : dto.createdAt().toString()
        );
    }

    @Transactional
    public CommentDto like(UUID currentUserId, UUID commentId) {
        Comment comment = getActiveComment(commentId);
        User user = getUser(currentUserId);
        if (commentLikeRepository.existsByComment_IdAndUser_Id(commentId, currentUserId)) {
            return commentMapper.toDto(comment, true);
        }
        commentLikeRepository.save(CommentLike.of(comment, user));
        comment.increaseLikeCount();
        eventPublisher.publishEvent(new CommentLikedEvent(
            comment.getId(),
            comment.getUser().getId(),
            user.getId(),
            user.getNickname(),
            comment.getArticle().getId(),
            comment.getArticle().getTitle(),
            comment.getContent(),
            comment.getCreatedAt()
        ));
        return commentMapper.toDto(comment, true);
    }

    @Transactional
    public CommentDto unlike(UUID currentUserId, UUID commentId) {
        Comment comment = getActiveComment(commentId);
        commentLikeRepository.findByComment_IdAndUser_Id(commentId, currentUserId)
            .ifPresent(like -> {
                commentLikeRepository.delete(like);
                comment.decreaseLikeCount();
                eventPublisher.publishEvent(new CommentUnlikedEvent(commentId, currentUserId));
            });
        return commentMapper.toDto(comment, false);
    }

    private Comment getActiveComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (comment.isDeleted()) {
            throw new CommentNotFoundException(commentId);
        }
        return comment;
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private static Instant parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(cursor);
        } catch (Exception e) {
            return null;
        }
    }

    private static Long parseLongCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
