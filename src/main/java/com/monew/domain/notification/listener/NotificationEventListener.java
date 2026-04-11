package com.monew.domain.notification.listener;

import com.monew.common.metrics.MonewMetrics;
import com.monew.domain.article.event.ArticleCollectedEvent;
import com.monew.domain.comment.event.CommentLikedEvent;
import com.monew.domain.interest.entity.Subscription;
import com.monew.domain.interest.repository.SubscriptionRepository;
import com.monew.domain.notification.entity.Notification.ResourceType;
import com.monew.domain.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;
    private final MonewMetrics metrics;

    @EventListener
    public void onCommentLiked(CommentLikedEvent event) {
        String content = "%s님이 회원님의 댓글을 좋아합니다.".formatted(event.likerNickname());
        notificationService.createForUser(
            event.commentOwnerId(),
            content,
            ResourceType.COMMENT,
            event.commentId()
        );
        metrics.incrementNotificationsCreated(1);
    }

    @EventListener
    public void onArticleCollected(ArticleCollectedEvent event) {
        List<Subscription> subscribers = subscriptionRepository.findAllByInterest_Id(event.interestId());
        if (subscribers.isEmpty()) {
            return;
        }
        String content = "[%s] 관심사 새 기사: %s".formatted(event.interestName(), event.articleTitle());
        int created = 0;
        for (Subscription subscription : subscribers) {
            try {
                notificationService.createForUser(
                    subscription.getUser().getId(),
                    content,
                    ResourceType.INTEREST,
                    event.interestId()
                );
                created++;
            } catch (Exception e) {
                log.warn("알림 생성 실패 userId={} articleId={}",
                    subscription.getUser().getId(), event.articleId(), e);
            }
        }
        metrics.incrementNotificationsCreated(created);
    }
}
