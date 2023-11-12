package org.runnect.server.user.exception.userException;

import lombok.Getter;
import org.runnect.server.common.exception.BasicException;
import org.runnect.server.common.constant.ErrorStatus;

@Getter
public class NotFoundUserException extends BasicException {
    public NotFoundUserException(ErrorStatus errorStatus, String message) {
        super(errorStatus, message);
    }
}
