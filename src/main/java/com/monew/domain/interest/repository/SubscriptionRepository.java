package com.monew.domain.interest.repository;

import com.monew.domain.interest.entity.Subscription;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByUser_IdAndInterest_Id(UUID userId, UUID interestId);

    boolean existsByUser_IdAndInterest_Id(UUID userId, UUID interestId);
}
