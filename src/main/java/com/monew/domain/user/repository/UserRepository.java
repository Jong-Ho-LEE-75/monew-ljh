package com.monew.domain.user.repository;

import com.monew.domain.user.entity.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("delete from User u where u.deleted = true and u.deletedAt < :threshold")
    int hardDeleteByDeletedAtBefore(@Param("threshold") Instant threshold);
}
