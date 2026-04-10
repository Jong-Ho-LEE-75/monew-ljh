package com.monew.domain.notification.exception;

import com.monew.common.exception.ErrorCode;
import com.monew.common.exception.MonewException;
import java.util.Map;

public class NotificationException extends MonewException {

    public NotificationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
