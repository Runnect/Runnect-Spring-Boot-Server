package org.runnect.server.record.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetRecordResponseDto {
    UserResponse user;
    List<RecordResponse> records;

    public static GetRecordResponseDto of(UserResponse user, List<RecordResponse> records) {
        return new GetRecordResponseDto(user, records);
    }
}
