package org.runnect.server.record.repository;

import java.util.Collection;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.record.entity.Record;
import org.runnect.server.user.entity.RunnectUser;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecordRepository extends Repository<Record, Long> {

    // CREATE
    void save(Record record);

    // READ

    // publicCourseId가 null일 때도 null값을 반환하기 위해서 LEFT JOIN FETCH 사용
    @Query("SELECT r FROM Record r JOIN FETCH r.course LEFT JOIN FETCH r.publicCourse WHERE r.runnectUser.id = :userId")
    List<Record> findAllByUserId(@Param("userId")Long userId);

    Optional<Record> findById(Long recordId);

    long countByRunnectUser(RunnectUser runnectUser);

    List<Record> findByIdIn(Collection<Long> ids);

    // DELETE
    long deleteByIdIn(Collection<Long> ids);

    @Modifying
    @Query("UPDATE Record r SET r.publicCourse = null WHERE r.publicCourse IN :publicCourses")
    int nullifyPublicCourseIn(@Param("publicCourses") Collection<PublicCourse> publicCourses);
}
