package org.runnect.server.scrap.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.scrap.entity.Scrap;
import org.runnect.server.user.entity.RunnectUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ScrapRepository extends Repository<Scrap, Long> {
    // CREATE
    void save(Scrap scrap);

    // READ
    @Query("SELECT s FROM Scrap s JOIN FETCH s.publicCourse WHERE s.runnectUser.id = :userId AND s.publicCourse.id = :publicCourseId")
    Optional<Scrap> findByUserIdAndPublicCourseId(@Param("userId") Long userId, @Param("publicCourseId") Long publicCourseId);

    @Query("SELECT s FROM Scrap s JOIN FETCH s.publicCourse pc JOIN FETCH pc.course c WHERE s.runnectUser.id = :userId AND s.scrapTF = true")
    Optional<List<Scrap>> findAllByUserIdAndScrapTF(@Param("userId") Long userId);

    long countByRunnectUser(RunnectUser runnectUser);

    Boolean existsByPublicCourseAndRunnectUser(PublicCourse publicCourse, RunnectUser runnectUser);

    @Query("select s.publicCourse.id from Scrap s where s.runnectUser = :runnectUser and s.scrapTF = true")
    List<Long> getScrappedTruePublicCourseIds(@Param("runnectUser") RunnectUser runnectUser);

    // DELETE
    Long deleteByPublicCourseIn(Collection<PublicCourse> publicCourses);

    Long countByPublicCourseAndScrapTFIsTrue(PublicCourse publicCourse);
}
