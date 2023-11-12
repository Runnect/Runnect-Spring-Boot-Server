package org.runnect.server.record.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateRecordResponseDto {
    UpdateRecordResponse record;

    public static UpdateRecordResponseDto of (UpdateRecordResponse response) {
        return new UpdateRecordResponseDto(response);
    }
}
