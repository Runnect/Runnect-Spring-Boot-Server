package org.runnect.server.user.exception.authException;

import lombok.Getter;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.BasicException;

@Getter
public class TimeExpiredRefreshTokenException extends BasicException {
    public TimeExpiredRefreshTokenException(ErrorStatus errorStatus, String message) {
        super(errorStatus, message);
    }
}

