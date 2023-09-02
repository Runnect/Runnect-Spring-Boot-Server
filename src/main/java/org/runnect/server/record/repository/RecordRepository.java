package org.runnect.server.record.repository;

import org.runnect.server.record.entity.Record;
import org.springframework.data.repository.Repository;

public interface RecordRepository extends Repository<Record, Long> {
    void save(Record record);
}
