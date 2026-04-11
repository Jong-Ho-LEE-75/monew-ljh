package com.monew.domain.activity.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

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
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserActivityProjectorTest {

    @Mock
    private UserActivityService userActivityService;

    @InjectMocks
    private UserActivityProjector projector;

    @Test
    void 사용자_등록_이벤트_초기화_호출() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        projector.onUserRegistered(new UserRegisteredEvent(userId, "u@u.com", "닉", now));

        verify(userActivityService).initializeOnUserCreated(eq(userId), eq("u@u.com"), eq("닉"), eq(now));
    }

    @Test
    void 사용자_소프트삭제_이벤트_삭제_호출() {
        UUID userId = UUID.randomUUID();
        projector.onUserSoftDeleted(new UserSoftDeletedEvent(userId));
        verify(userActivityService).deleteOnUserSoftDeleted(userId);
    }

    @Test
    void 댓글_생성_이벤트_프로젝션() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        projector.onCommentCreated(new CommentCreatedEvent(
            commentId, UUID.randomUUID(), "기사제목", userId, "닉", "내용", Instant.now()));
        verify(userActivityService).projectCommentCreated(eq(userId), any(CommentSnapshot.class));
    }

    @Test
    void 댓글_좋아요_이벤트_프로젝션() {
        UUID likerId = UUID.randomUUID();
        projector.onCommentLiked(new CommentLikedEvent(
            UUID.randomUUID(), UUID.randomUUID(), likerId, "라이커"));
        verify(userActivityService).projectCommentLiked(eq(likerId), any(CommentLikeSnapshot.class));
    }

    @Test
    void 댓글_좋아요_취소_이벤트_프로젝션() {
        UUID likerId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        projector.onCommentUnliked(new CommentUnlikedEvent(commentId, likerId));
        verify(userActivityService).projectCommentUnliked(likerId, commentId);
    }

    @Test
    void 구독_추가_이벤트_프로젝션() {
        UUID userId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        projector.onSubscriptionAdded(new SubscriptionAddedEvent(
            userId, interestId, "관심사", List.of("k"), 5L, Instant.now()));
        verify(userActivityService).projectSubscriptionAdded(eq(userId), any(SubscriptionSnapshot.class));
    }

    @Test
    void 구독_해제_이벤트_프로젝션() {
        UUID userId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        projector.onSubscriptionRemoved(new SubscriptionRemovedEvent(userId, interestId));
        verify(userActivityService).projectSubscriptionRemoved(userId, interestId);
    }

    @Test
    void 기사_조회_이벤트_프로젝션() {
        UUID userId = UUID.randomUUID();
        projector.onArticleViewed(new ArticleViewedEvent(
            UUID.randomUUID(), UUID.randomUUID(), userId,
            "NAVER", "url", "title", "summary",
            Instant.now(), 10L, 0L, Instant.now()));
        verify(userActivityService).projectArticleViewed(eq(userId), any(ArticleViewSnapshot.class));
    }

    @Test
    void 프로젝션_실패시_예외_삼킴() {
        UUID userId = UUID.randomUUID();
        doThrow(new RuntimeException("mongo down"))
            .when(userActivityService).deleteOnUserSoftDeleted(any());

        projector.onUserSoftDeleted(new UserSoftDeletedEvent(userId));

        verify(userActivityService).deleteOnUserSoftDeleted(userId);
    }
}
