package com.monew.domain.notification.dto;

import com.monew.domain.notification.entity.Notification.ResourceType;
import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    String content,
    ResourceType resourceType,
    UUID resourceId,
    boolean confirmed,
    Instant createdAt
) {

}
