package org.runnect.server.publicCourse.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.user.entity.RunnectUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublicCourseRepository  extends JpaRepository<PublicCourse, Long> {
    // CREATE

    // READ
    @Query("SELECT pc FROM PublicCourse pc JOIN FETCH pc.course WHERE pc.id = :publicCourseId")
    Optional<PublicCourse> findById(@Param("publicCourseId") Long publicCourseId);

    @Query("select count(pc.id) from PublicCourse pc where pc.runnectUser = :user")
    Long countPublicCourseByUser(@Param("user") RunnectUser user);

    @Query("select pc from PublicCourse pc join fetch pc.course where pc.runnectUser = :user")
    List<PublicCourse> findPublicCoursesByRunnectUser(@Param("user") RunnectUser user);

    List<PublicCourse> findByIdIn(Collection<Long> ids);

    // DELETE
}
