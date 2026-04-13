package com.monew.domain.activity.service;

import com.monew.domain.activity.document.UserActivity;
import com.monew.domain.activity.document.UserActivity.ArticleViewSnapshot;
import com.monew.domain.activity.document.UserActivity.CommentLikeSnapshot;
import com.monew.domain.activity.document.UserActivity.CommentSnapshot;
import com.monew.domain.activity.document.UserActivity.SubscriptionSnapshot;
import com.monew.domain.activity.exception.UserActivityNotFoundException;
import com.monew.domain.activity.repository.UserActivityRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityService {

    static final int RECENT_LIMIT = 10;

    private final UserActivityRepository userActivityRepository;

    public UserActivity getActivity(UUID userId) {
        UserActivity activity = userActivityRepository.findByUserId(userId)
            .orElseThrow(() -> new UserActivityNotFoundException(userId));
        Set<UUID> likedCommentIds = activity.getRecentCommentLikes().stream()
            .map(CommentLikeSnapshot::getCommentId)
            .collect(java.util.stream.Collectors.toSet());
        for (CommentSnapshot c : activity.getRecentComments()) {
            c.setLikedByMe(likedCommentIds.contains(c.getId()));
        }
        return activity;
    }

    public UserActivity initializeOnUserCreated(UUID userId, String email, String nickname, Instant createdAt) {
        Optional<UserActivity> existing = userActivityRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        UserActivity activity = UserActivity.builder()
            .userId(userId)
            .email(email)
            .nickname(nickname)
            .userCreatedAt(createdAt)
            .build();
        return userActivityRepository.save(activity);
    }

    public void deleteOnUserSoftDeleted(UUID userId) {
        userActivityRepository.deleteByUserId(userId);
    }

    public void projectSubscriptionAdded(UUID userId, SubscriptionSnapshot snapshot) {
        UserActivity activity = getOrSkip(userId);
        if (activity == null) {
            return;
        }
        List<SubscriptionSnapshot> list = new ArrayList<>(activity.getSubscriptions());
        list.removeIf(s -> s.getInterestId().equals(snapshot.getInterestId()));
        list.add(snapshot);
        activity.getSubscriptions().clear();
        activity.getSubscriptions().addAll(list);
        userActivityRepository.save(activity);
    }

    public void projectSubscriptionRemoved(UUID userId, UUID interestId) {
        UserActivity activity = getOrSkip(userId);
        if (activity == null) {
            return;
        }
        activity.getSubscriptions().removeIf(s -> s.getInterestId().equals(interestId));
        userActivityRepository.save(activity);
    }

    public void projectCommentCreated(UUID userId, CommentSnapshot snapshot) {
        UserActivity activity = getOrSkip(userId);
        if (activity == null) {
            return;
        }
        activity.getRecentComments().removeIf(c -> c.getId().equals(snapshot.getId()));
        activity.getRecentComments().add(snapshot);
        trimRecent(activity.getRecentComments(), CommentSnapshot::getCreatedAt);
        userActivityRepository.save(activity);
    }

    public void projectCommentLiked(UUID userId, CommentLikeSnapshot snapshot) {
        UserActivity activity = getOrSkip(userId);
        if (activity == null) {
            return;
        }
        activity.getRecentCommentLikes().removeIf(c -> c.getCommentId().equals(snapshot.getCommentId()));
        activity.getRecentCommentLikes().add(snapshot);
        trimRecent(activity.getRecentCommentLikes(), CommentLikeSnapshot::getCreatedAt);
        userActivityRepository.save(activity);
    }

    public void updateCommentLikeCount(UUID commentOwnerId, UUID commentId, long likeCount) {
        UserActivity activity = getOrSkip(commentOwnerId);
        if (activity == null) {
            return;
        }
        for (CommentSnapshot c : activity.getRecentComments()) {
            if (c.getId().equals(commentId)) {
                c.setLikeCount(likeCount);
                userActivityRepository.save(activity);
                return;
            }
        }
    }

    public void projectCommentUnliked(UUID userId, UUID commentId) {
        UserActivity activity = getOrSkip(userId);
        if (activity == null) {
            return;
        }
        activity.getRecentCommentLikes().removeIf(c -> c.getCommentId().equals(commentId));
        userActivityRepository.save(activity);
    }

    public void projectArticleViewed(UUID userId, ArticleViewSnapshot snapshot) {
        UserActivity activity = getOrSkip(userId);
        if (activity == null) {
            return;
        }
        activity.getRecentViewedArticles().removeIf(v -> v.getArticleId().equals(snapshot.getArticleId()));
        activity.getRecentViewedArticles().add(snapshot);
        trimRecent(activity.getRecentViewedArticles(), ArticleViewSnapshot::getCreatedAt);
        userActivityRepository.save(activity);
    }

    private UserActivity getOrSkip(UUID userId) {
        Optional<UserActivity> found = userActivityRepository.findByUserId(userId);
        if (found.isEmpty()) {
            log.debug("UserActivity 문서 없음, 프로젝션 스킵 userId={}", userId);
            return null;
        }
        return found.get();
    }

    private static <T> void trimRecent(List<T> list, java.util.function.Function<T, Instant> createdAtExtractor) {
        list.sort(Comparator.comparing(createdAtExtractor).reversed());
        while (list.size() > RECENT_LIMIT) {
            list.remove(list.size() - 1);
        }
    }
}
