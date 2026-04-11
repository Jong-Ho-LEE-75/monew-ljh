package com.monew.domain.activity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.monew.domain.activity.document.UserActivity;
import com.monew.domain.activity.document.UserActivity.ArticleViewSnapshot;
import com.monew.domain.activity.document.UserActivity.CommentLikeSnapshot;
import com.monew.domain.activity.document.UserActivity.CommentSnapshot;
import com.monew.domain.activity.document.UserActivity.SubscriptionSnapshot;
import com.monew.domain.activity.exception.UserActivityNotFoundException;
import com.monew.domain.activity.repository.UserActivityRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserActivityServiceTest {

    @Mock
    private UserActivityRepository userActivityRepository;

    @InjectMocks
    private UserActivityService service;

    @Test
    void initializeOnUserCreated_신규_생성() {
        UUID userId = UUID.randomUUID();
        given(userActivityRepository.findByUserId(userId)).willReturn(Optional.empty());
        given(userActivityRepository.save(any(UserActivity.class))).willAnswer(inv -> inv.getArgument(0));

        UserActivity activity = service.initializeOnUserCreated(
            userId, "u@u.com", "유저", Instant.now());

        assertThat(activity.getUserId()).isEqualTo(userId);
        assertThat(activity.getNickname()).isEqualTo("유저");
    }

    @Test
    void initializeOnUserCreated_이미_존재하면_재사용() {
        UUID userId = UUID.randomUUID();
        UserActivity existing = UserActivity.builder()
            .userId(userId).email("u@u.com").nickname("기존").build();

        given(userActivityRepository.findByUserId(userId)).willReturn(Optional.of(existing));

        UserActivity result = service.initializeOnUserCreated(userId, "u@u.com", "변경", Instant.now());

        assertThat(result).isSameAs(existing);
        verify(userActivityRepository, never()).save(any());
    }

    @Test
    void getActivity_없으면_예외() {
        UUID userId = UUID.randomUUID();
        given(userActivityRepository.findByUserId(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getActivity(userId))
            .isInstanceOf(UserActivityNotFoundException.class);
    }

    @Test
    void projectCommentCreated_10건_초과시_오래된것_제거() {
        UUID userId = UUID.randomUUID();
        List<CommentSnapshot> comments = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            comments.add(CommentSnapshot.builder()
                .id(UUID.randomUUID())
                .content("c" + i)
                .createdAt(Instant.now().minusSeconds(100 - i))
                .build());
        }
        UserActivity activity = UserActivity.builder()
            .userId(userId)
            .recentComments(comments)
            .build();

        given(userActivityRepository.findByUserId(userId)).willReturn(Optional.of(activity));
        given(userActivityRepository.save(any(UserActivity.class))).willAnswer(inv -> inv.getArgument(0));

        CommentSnapshot latest = CommentSnapshot.builder()
            .id(UUID.randomUUID())
            .content("최신")
            .createdAt(Instant.now())
            .build();

        service.projectCommentCreated(userId, latest);

        assertThat(activity.getRecentComments()).hasSize(10);
        assertThat(activity.getRecentComments().get(0).getContent()).isEqualTo("최신");
    }

    @Test
    void projectCommentLiked_동일_댓글은_중복_제거_후_추가() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        Instant oldTimestamp = Instant.now().minusSeconds(100);
        List<CommentLikeSnapshot> likes = new ArrayList<>();
        likes.add(CommentLikeSnapshot.builder()
            .id(UUID.randomUUID())
            .commentId(commentId)
            .createdAt(oldTimestamp)
            .build());

        UserActivity activity = UserActivity.builder()
            .userId(userId)
            .recentCommentLikes(likes)
            .build();

        given(userActivityRepository.findByUserId(userId)).willReturn(Optional.of(activity));
        given(userActivityRepository.save(any(UserActivity.class))).willAnswer(inv -> inv.getArgument(0));

        UUID newId = UUID.randomUUID();
        CommentLikeSnapshot updated = CommentLikeSnapshot.builder()
            .id(newId)
            .commentId(commentId)
            .createdAt(Instant.now())
            .build();

        service.projectCommentLiked(userId, updated);

        assertThat(activity.getRecentCommentLikes()).hasSize(1);
        assertThat(activity.getRecentCommentLikes().get(0).getId()).isEqualTo(newId);
        assertThat(activity.getRecentCommentLikes().get(0).getCreatedAt()).isAfter(oldTimestamp);
    }

    @Test
    void projectSubscriptionAdded_동일_관심사_교체() {
        UUID userId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        SubscriptionSnapshot original = SubscriptionSnapshot.builder()
            .interestId(interestId)
            .interestName("old")
            .interestSubscriberCount(5)
            .build();
        List<SubscriptionSnapshot> subs = new ArrayList<>();
        subs.add(original);

        UserActivity activity = UserActivity.builder()
            .userId(userId)
            .subscriptions(subs)
            .build();

        given(userActivityRepository.findByUserId(userId)).willReturn(Optional.of(activity));
        given(userActivityRepository.save(any(UserActivity.class))).willAnswer(inv -> inv.getArgument(0));

        SubscriptionSnapshot refreshed = SubscriptionSnapshot.builder()
            .interestId(interestId)
            .interestName("new")
            .interestSubscriberCount(6)
            .build();

        service.projectSubscriptionAdded(userId, refreshed);

        assertThat(activity.getSubscriptions()).hasSize(1);
        assertThat(activity.getSubscriptions().get(0).getInterestName()).isEqualTo("new");
    }

    @Test
    void projectArticleViewed_문서없으면_저장_호출_안함() {
        UUID userId = UUID.randomUUID();
        given(userActivityRepository.findByUserId(userId)).willReturn(Optional.empty());

        service.projectArticleViewed(userId, ArticleViewSnapshot.builder()
            .articleId(UUID.randomUUID())
            .createdAt(Instant.now())
            .build());

        verify(userActivityRepository, never()).save(any());
    }
}
