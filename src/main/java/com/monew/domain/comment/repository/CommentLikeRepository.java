package com.monew.domain.comment.repository;

import com.monew.domain.comment.entity.CommentLike;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

    Optional<CommentLike> findByComment_IdAndUser_Id(UUID commentId, UUID userId);

    boolean existsByComment_IdAndUser_Id(UUID commentId, UUID userId);
}
