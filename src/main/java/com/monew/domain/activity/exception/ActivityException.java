package com.monew.domain.activity.exception;

import com.monew.common.exception.ErrorCode;
import com.monew.common.exception.MonewException;
import java.util.Map;

public class ActivityException extends MonewException {

    public ActivityException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
