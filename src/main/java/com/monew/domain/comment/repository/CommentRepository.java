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
        order by c.createdAt desc
        """)
    List<Comment> findFirstPageByArticle(
        @Param("articleId") UUID articleId,
        Pageable pageable
    );

    @Query("""
        select c from Comment c
        where c.article.id = :articleId
          and c.deleted = false
          and c.createdAt < :cursor
        order by c.createdAt desc
        """)
    List<Comment> findPageByArticleAfter(
        @Param("articleId") UUID articleId,
        @Param("cursor") Instant cursor,
        Pageable pageable
    );

    @Query("""
        select c from Comment c
        where c.article.id = :articleId
          and c.deleted = false
        order by c.likeCount desc, c.createdAt desc
        """)
    List<Comment> findFirstPageByArticleOrderByLikes(
        @Param("articleId") UUID articleId,
        Pageable pageable
    );

    @Query("""
        select c from Comment c
        where c.article.id = :articleId
          and c.deleted = false
          and c.likeCount < :cursor
        order by c.likeCount desc, c.createdAt desc
        """)
    List<Comment> findPageByArticleAfterLikes(
        @Param("articleId") UUID articleId,
        @Param("cursor") Long cursor,
        Pageable pageable
    );
}
