package org.runnect.server.common.exception;

import org.runnect.server.common.constant.ErrorStatus;

public class PermissionDeniedException extends BasicException {

    public PermissionDeniedException(ErrorStatus errorStatus, String message) {
        super(errorStatus, message);
    }
}
