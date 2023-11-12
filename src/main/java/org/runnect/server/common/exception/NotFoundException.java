package org.runnect.server.common.exception;

import org.runnect.server.common.constant.ErrorStatus;

public class NotFoundException extends BasicException {
    public NotFoundException(ErrorStatus errorStatus, String message) {
        super(errorStatus, message);
    }
}
