package com.monew.domain.notification.exception;

import com.monew.common.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class NotificationNotFoundException extends NotificationException {

    public NotificationNotFoundException(UUID id) {
        super(ErrorCode.NOTIFICATION_NOT_FOUND, Map.of("id", id));
    }
}
