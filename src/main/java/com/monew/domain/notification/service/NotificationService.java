package com.monew.domain.notification.service;

import com.monew.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // TODO: findUnconfirmed(userId, cursor), confirm(id, userId), confirmAll(userId),
    //       createInterestNotification, createCommentLikeNotification
}
