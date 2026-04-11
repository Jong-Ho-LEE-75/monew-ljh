package com.monew.domain.comment.repository;

import com.monew.domain.comment.entity.Comment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("""
        select c from Comment c
        where c.article.id = :articleId
          and c.deleted = false
          and (:cursor is null or c.createdAt < :cursor)
        order by c.createdAt desc
        """)
    List<Comment> findPageByArticle(
        @Param("articleId") UUID articleId,
        @Param("cursor") Instant cursor,
        Pageable pageable
    );
}
