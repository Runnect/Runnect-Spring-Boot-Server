package org.runnect.server.common.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SortStatus {
    SCRAP_DESC("scrap","scrapCount"),
    DATE_DESC("date","createdAt");

    private final String value;
    private final String property;
}
