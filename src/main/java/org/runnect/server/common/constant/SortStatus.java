package org.runnect.server.common.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SortStatus {
    SCRAP_DESC("scrap"),
    DATE_DESC("date");
    private final String vlaue;
}
