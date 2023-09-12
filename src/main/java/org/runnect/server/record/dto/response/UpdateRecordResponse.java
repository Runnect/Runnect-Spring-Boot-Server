package org.runnect.server.record.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateRecordResponse {
    private Long id;
    private String title;

    public static UpdateRecordResponse of (Long id, String title) {
        return new UpdateRecordResponse(id, title);
    }
}
