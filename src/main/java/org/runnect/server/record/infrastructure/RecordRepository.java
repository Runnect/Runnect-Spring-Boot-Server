package org.runnect.server.record.infrastructure;

import org.runnect.server.record.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

public interface RecordRepository extends Repository<Record, Long> {
    void save(Record record);
}
