package org.runnect.server.user.exception.userException;

import org.runnect.server.common.exception.BasicException;
import org.runnect.server.common.constant.ErrorStatus;

public class DuplicateNicknameException extends BasicException {

    public DuplicateNicknameException(final ErrorStatus errorStatus, final String message) {
        super(errorStatus, message);
    }
}
