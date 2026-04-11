package com.monew.domain.user.exception;

import com.monew.common.exception.ErrorCode;
import java.util.Map;

public class InvalidPasswordException extends UserException {

    public InvalidPasswordException(String email) {
        super(ErrorCode.INVALID_PASSWORD, Map.of("email", email));
    }
}
