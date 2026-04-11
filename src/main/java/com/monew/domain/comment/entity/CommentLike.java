package com.monew.domain.comment.entity;

import com.monew.common.entity.BaseEntity;
import com.monew.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "comment_likes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private CommentLike(Comment comment, User user) {
        this.comment = comment;
        this.user = user;
    }

    public static CommentLike of(Comment comment, User user) {
        return new CommentLike(comment, user);
    }
}
