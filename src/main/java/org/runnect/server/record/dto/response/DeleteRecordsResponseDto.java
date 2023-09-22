package org.runnect.server.record.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteRecordsResponseDto {

    private Long deletedRecordIdCount;

    public static DeleteRecordsResponseDto from(final Long deletedRecordIdCount) {
        return new DeleteRecordsResponseDto(deletedRecordIdCount);
    }
}
