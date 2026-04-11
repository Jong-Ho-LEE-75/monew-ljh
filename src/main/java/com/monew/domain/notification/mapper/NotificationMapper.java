package com.monew.domain.notification.mapper;

import com.monew.domain.notification.dto.NotificationDto;
import com.monew.domain.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification notification) {
        return new NotificationDto(
            notification.getId(),
            notification.getContent(),
            notification.getResourceType(),
            notification.getResourceId(),
            notification.isConfirmed(),
            notification.getCreatedAt()
        );
    }
}
