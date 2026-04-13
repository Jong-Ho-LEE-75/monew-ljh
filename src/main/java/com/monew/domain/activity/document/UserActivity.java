package com.monew.domain.activity.document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_activities")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserActivity {

    @Id
    private String id;

    private UUID userId;
    private String email;
    private String nickname;
    private Instant userCreatedAt;

    @Builder.Default
    private List<SubscriptionSnapshot> subscriptions = new ArrayList<>();

    @Builder.Default
    private List<CommentSnapshot> recentComments = new ArrayList<>();

    @Builder.Default
    private List<CommentLikeSnapshot> recentCommentLikes = new ArrayList<>();

    @Builder.Default
    private List<ArticleViewSnapshot> recentViewedArticles = new ArrayList<>();

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SubscriptionSnapshot {
        private UUID interestId;
        private String interestName;
        private List<String> interestKeywords;
        private long interestSubscriberCount;
        private Instant createdAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CommentSnapshot {
        private UUID id;
        private UUID articleId;
        private String articleTitle;
        private UUID userId;
        private String userNickname;
        private String content;
        private long likeCount;
        private Instant createdAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CommentLikeSnapshot {
        private UUID id;
        private Instant createdAt;
        private UUID commentId;
        private UUID articleId;
        private String articleTitle;
        private UUID commentUserId;
        private String commentUserNickname;
        private String commentContent;
        private long commentLikeCount;
        private Instant commentCreatedAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ArticleViewSnapshot {
        private UUID id;
        private UUID viewedBy;
        private Instant createdAt;
        private UUID articleId;
        private String source;
        private String sourceUrl;
        private String articleTitle;
        private Instant articlePublishedDate;
        private String articleSummary;
        private long articleCommentCount;
        private long articleViewCount;
    }
}
