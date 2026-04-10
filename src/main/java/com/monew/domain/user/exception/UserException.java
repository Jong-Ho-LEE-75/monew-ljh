package com.monew.domain.user.exception;

import com.monew.common.exception.ErrorCode;
import com.monew.common.exception.MonewException;
import java.util.Map;

public class UserException extends MonewException {

    public UserException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
