package com.monew.domain.activity.listener;

import com.monew.domain.activity.document.UserActivity.ArticleViewSnapshot;
import com.monew.domain.activity.document.UserActivity.CommentLikeSnapshot;
import com.monew.domain.activity.document.UserActivity.CommentSnapshot;
import com.monew.domain.activity.document.UserActivity.SubscriptionSnapshot;
import com.monew.domain.activity.service.UserActivityService;
import com.monew.domain.article.event.ArticleViewedEvent;
import com.monew.domain.comment.event.CommentCreatedEvent;
import com.monew.domain.comment.event.CommentLikedEvent;
import com.monew.domain.comment.event.CommentUnlikedEvent;
import com.monew.domain.interest.event.SubscriptionAddedEvent;
import com.monew.domain.interest.event.SubscriptionRemovedEvent;
import com.monew.domain.user.event.UserRegisteredEvent;
import com.monew.domain.user.event.UserSoftDeletedEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityProjector {

    private final UserActivityService userActivityService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        safely("initializeOnUserCreated", () ->
            userActivityService.initializeOnUserCreated(
                event.userId(), event.email(), event.nickname(), event.createdAt()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserSoftDeleted(UserSoftDeletedEvent event) {
        safely("deleteOnUserSoftDeleted", () ->
            userActivityService.deleteOnUserSoftDeleted(event.userId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentCreated(CommentCreatedEvent event) {
        safely("projectCommentCreated", () -> userActivityService.projectCommentCreated(
            event.userId(),
            CommentSnapshot.builder()
                .id(event.commentId())
                .articleId(event.articleId())
                .articleTitle(event.articleTitle())
                .userId(event.userId())
                .userNickname(event.userNickname())
                .content(event.content())
                .likeCount(0)
                .createdAt(event.createdAt())
                .build()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentLiked(CommentLikedEvent event) {
        safely("projectCommentLiked", () -> userActivityService.projectCommentLiked(
            event.likerId(),
            CommentLikeSnapshot.builder()
                .id(UUID.randomUUID())
                .commentId(event.commentId())
                .articleId(event.articleId())
                .articleTitle(event.articleTitle())
                .commentUserId(event.commentOwnerId())
                .commentUserNickname(event.likerNickname())
                .commentContent(event.commentContent())
                .commentCreatedAt(event.commentCreatedAt())
                .createdAt(java.time.Instant.now())
                .build()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentUnliked(CommentUnlikedEvent event) {
        safely("projectCommentUnliked", () ->
            userActivityService.projectCommentUnliked(event.likerId(), event.commentId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubscriptionAdded(SubscriptionAddedEvent event) {
        safely("projectSubscriptionAdded", () -> userActivityService.projectSubscriptionAdded(
            event.userId(),
            SubscriptionSnapshot.builder()
                .interestId(event.interestId())
                .interestName(event.interestName())
                .interestKeywords(event.interestKeywords())
                .interestSubscriberCount(event.subscriberCount())
                .createdAt(event.createdAt())
                .build()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubscriptionRemoved(SubscriptionRemovedEvent event) {
        safely("projectSubscriptionRemoved", () ->
            userActivityService.projectSubscriptionRemoved(event.userId(), event.interestId()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onArticleViewed(ArticleViewedEvent event) {
        safely("projectArticleViewed", () -> userActivityService.projectArticleViewed(
            event.userId(),
            ArticleViewSnapshot.builder()
                .id(event.articleViewId())
                .viewedBy(event.userId())
                .createdAt(event.viewedAt())
                .articleId(event.articleId())
                .source(event.source())
                .sourceUrl(event.sourceUrl())
                .articleTitle(event.articleTitle())
                .articleSummary(event.articleSummary())
                .articlePublishedDate(event.articlePublishedAt())
                .articleViewCount(event.articleViewCount())
                .articleCommentCount(event.articleCommentCount())
                .build()
        ));
    }

    private void safely(String operation, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.warn("UserActivity 프로젝션 실패 operation={}", operation, e);
        }
    }
}
