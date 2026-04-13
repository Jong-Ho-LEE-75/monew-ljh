package com.monew.domain.notification.service;

import com.monew.common.dto.CursorRequest;
import com.monew.common.dto.PageResponse;
import com.monew.domain.notification.dto.NotificationDto;
import com.monew.domain.notification.entity.Notification;
import com.monew.domain.notification.entity.Notification.ResourceType;
import com.monew.domain.notification.exception.NotificationNotFoundException;
import com.monew.domain.notification.mapper.NotificationMapper;
import com.monew.domain.notification.repository.NotificationRepository;
import com.monew.domain.user.entity.User;
import com.monew.domain.user.exception.UserNotFoundException;
import com.monew.domain.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;

    public PageResponse<NotificationDto> findUnconfirmed(UUID userId, CursorRequest cursorRequest) {
        int size = cursorRequest.sizeOrDefault();
        Instant cursor = parseCursor(cursorRequest.cursor());
        Pageable pageable = PageRequest.of(0, size + 1);
        List<Notification> page = cursor == null
            ? notificationRepository.findFirstUnconfirmedPage(userId, pageable)
            : notificationRepository.findUnconfirmedPageAfter(userId, cursor, pageable);
        List<NotificationDto> dtos = page.stream().map(notificationMapper::toDto).toList();
        long totalElements = notificationRepository.countByUserIdAndConfirmedFalse(userId);
        return PageResponse.of(dtos, size, dto -> dto.createdAt().toString(), totalElements);
    }

    @Transactional
    public void confirm(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new NotificationNotFoundException(notificationId);
        }
        notification.confirm();
    }

    @Transactional
    public int confirmAll(UUID userId) {
        return notificationRepository.confirmAllByUserId(userId);
    }

    @Transactional
    public int deleteConfirmedBefore(Instant threshold) {
        return notificationRepository.deleteConfirmedBefore(threshold);
    }

    @Transactional
    public NotificationDto createForUser(
        UUID userId,
        String content,
        ResourceType resourceType,
        UUID resourceId
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        Notification notification = Notification.builder()
            .user(user)
            .content(content)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .build();
        return notificationMapper.toDto(notificationRepository.save(notification));
    }

    private static Instant parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(cursor);
        } catch (Exception e) {
            return null;
        }
    }
}
