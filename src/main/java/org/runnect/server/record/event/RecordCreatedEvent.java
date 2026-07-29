package org.runnect.server.record.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecordCreatedEvent {
    private Long userId;
    private Long recordId;
}
