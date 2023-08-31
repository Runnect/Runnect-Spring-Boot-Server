package org.runnect.server.record.infrastructure;

import org.runnect.server.record.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<Record, Long>  {
}
