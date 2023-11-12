package org.runnect.server.record.dto.request;

import java.util.List;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteRecordsRequestDto {

    @Size(min = 1)
    private List<Long> recordIdList;
}
