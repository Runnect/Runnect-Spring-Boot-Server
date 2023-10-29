package org.runnect.server.user.exception.authException;

import lombok.Getter;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.BasicException;

@Getter
public class TimeExpiredAccessTokenException extends BasicException {
    public TimeExpiredAccessTokenException(ErrorStatus errorStatus, String message) {
        super(errorStatus, message);
    }
}

