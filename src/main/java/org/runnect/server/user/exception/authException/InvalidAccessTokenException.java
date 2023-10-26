package org.runnect.server.user.exception.authException;

import lombok.Getter;
import org.runnect.server.common.exception.BasicException;
import org.runnect.server.common.constant.ErrorStatus;

@Getter
public class InvalidAccessTokenException extends BasicException {
    public InvalidAccessTokenException(ErrorStatus errorStatus, String message) {
        super(errorStatus, message);
    }
}
