package org.runnect.server.record.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateRecordDto {
    private Long id;
    private String createdAt;

    public CreateRecordDto(Long id, String createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }
}
