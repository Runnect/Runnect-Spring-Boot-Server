package org.runnect.server.common.exception;

public class PermissionDeniedException extends BasicException {

    public PermissionDeniedException(ErrorStatus errorStatus, String message) {
        super(errorStatus, message);
    }
}
