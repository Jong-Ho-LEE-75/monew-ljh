package com.monew.domain.notification.repository;

import com.monew.domain.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("""
        select n from Notification n
        where n.user.id = :userId
          and n.confirmed = false
        order by n.createdAt desc
        """)
    List<Notification> findFirstUnconfirmedPage(
        @Param("userId") UUID userId,
        Pageable pageable
    );

    @Query("""
        select n from Notification n
        where n.user.id = :userId
          and n.confirmed = false
          and n.createdAt < :cursor
        order by n.createdAt desc
        """)
    List<Notification> findUnconfirmedPageAfter(
        @Param("userId") UUID userId,
        @Param("cursor") Instant cursor,
        Pageable pageable
    );

    @Modifying
    @Query("update Notification n set n.confirmed = true where n.user.id = :userId and n.confirmed = false")
    int confirmAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("delete from Notification n where n.confirmed = true and n.updatedAt < :threshold")
    int deleteConfirmedBefore(@Param("threshold") Instant threshold);
}
