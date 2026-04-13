package com.monew.domain.notification.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.monew.common.metrics.MonewMetrics;
import com.monew.domain.article.event.ArticleCollectedEvent;
import com.monew.domain.comment.event.CommentLikedEvent;
import com.monew.domain.interest.entity.Interest;
import com.monew.domain.interest.entity.Subscription;
import com.monew.domain.interest.repository.SubscriptionRepository;
import com.monew.domain.notification.entity.Notification.ResourceType;
import com.monew.domain.notification.service.NotificationService;
import com.monew.domain.user.entity.User;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private NotificationService notificationService;

    @Spy
    private MonewMetrics metrics = new MonewMetrics(new SimpleMeterRegistry());

    @InjectMocks
    private NotificationEventListener listener;

    @Test
    void 댓글_좋아요_이벤트_처리() {
        UUID commentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID likerId = UUID.randomUUID();

        listener.onCommentLiked(new CommentLikedEvent(commentId, ownerId, likerId, "라이커",
            UUID.randomUUID(), "기사 제목", "댓글 내용", java.time.Instant.now()));

        verify(notificationService).createForUser(
            eq(ownerId),
            any(String.class),
            eq(ResourceType.COMMENT),
            eq(commentId)
        );
    }

    @Test
    void 기사_수집_이벤트_구독자에게_알림() throws Exception {
        UUID interestId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        User userA = User.builder().email("a@a.com").nickname("a").password("p").build();
        User userB = User.builder().email("b@b.com").nickname("b").password("p").build();
        setId(userA, UUID.randomUUID());
        setId(userB, UUID.randomUUID());

        Interest interest = Interest.builder().name("Spring").keywords(List.of("spring")).build();
        setId(interest, interestId);

        Subscription s1 = Subscription.of(userA, interest);
        Subscription s2 = Subscription.of(userB, interest);

        given(subscriptionRepository.findAllByInterest_Id(interestId))
            .willReturn(List.of(s1, s2));

        listener.onArticleCollected(new ArticleCollectedEvent(articleId, interestId, "Spring", "제목"));

        verify(notificationService, times(2)).createForUser(
            any(UUID.class), any(String.class), eq(ResourceType.INTEREST), eq(interestId));
    }

    @Test
    void 구독자_없으면_알림_생성_안함() {
        UUID interestId = UUID.randomUUID();
        given(subscriptionRepository.findAllByInterest_Id(interestId)).willReturn(List.of());

        listener.onArticleCollected(new ArticleCollectedEvent(
            UUID.randomUUID(), interestId, "Spring", "제목"));

        verify(notificationService, never()).createForUser(
            any(), any(), any(), any());
    }

    private static void setId(Object target, UUID id) throws Exception {
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField("id");
                field.setAccessible(true);
                field.set(target, id);
                return;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new IllegalStateException("id field not found");
    }
}
