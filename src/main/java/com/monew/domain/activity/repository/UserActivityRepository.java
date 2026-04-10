package com.monew.domain.activity.repository;

import com.monew.domain.activity.document.UserActivity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserActivityRepository extends MongoRepository<UserActivity, String> {

    Optional<UserActivity> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
