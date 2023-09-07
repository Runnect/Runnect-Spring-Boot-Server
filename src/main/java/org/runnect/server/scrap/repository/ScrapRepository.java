package org.runnect.server.scrap.repository;

import org.runnect.server.scrap.entity.Scrap;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface ScrapRepository extends Repository<Scrap, Long> {
    // CREATE
    void save(Scrap scrap);

    // READ
    @Query("SELECT s FROM Scrap s JOIN FETCH s.publicCourse WHERE s.runnectUser.id = :userId AND s.publicCourse.id = :publicCourseId")
    Optional<Scrap> findByUserIdAndPublicCourseId(Long userId, Long publicCourseId);

    // DELETE
}