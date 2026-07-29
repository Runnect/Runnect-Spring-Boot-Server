package org.runnect.server.record.event;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedRecordEventRepository extends JpaRepository<ProcessedRecordEvent, Long> {
}
