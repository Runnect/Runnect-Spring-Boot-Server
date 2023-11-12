package org.runnect.server.record.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateRecordRequestDto {
    @NotEmpty(message = "수정할 제목이 없습니다.")
    private String title;
}
