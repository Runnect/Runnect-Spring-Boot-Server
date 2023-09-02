package org.runnect.server.record.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateRecordResponseDto {
    private CreateRecordDto record;
    public CreateRecordResponseDto(CreateRecordDto record) {
        this.record = record;
    }

}
