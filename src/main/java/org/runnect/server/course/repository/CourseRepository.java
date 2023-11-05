package org.runnect.server.course.repository;


import java.util.List;
import org.runnect.server.course.entity.Course;
import org.runnect.server.user.entity.RunnectUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // CREATE
    Course save(Course course);

    // READ
    @Query("select c from Course c join fetch c.runnectUser where c.runnectUser.id = :userId and c.deletedAt = null order by c.createdAt desc")
    List<Course> findCourseByUserId(Long userId);

    @Query("select c from Course c join fetch c.runnectUser where c.id = :courseId")
    Optional<Course> findCourseByIdFetchUser(Long courseId);

    @Query("select c from Course c join fetch c.runnectUser where c.runnectUser.id = :userId and c.isPrivate = true and c.deletedAt = null order by c.createdAt desc")
    List<Course> findCourseByUserIdOnlyPrivate(Long userId);

    long countByRunnectUser(RunnectUser runnectUser);

    Optional<Course> findById(Long courseId);

    @Query("select c from Course c where c.id = :courseId and c.runnectUser.id = :userId and c.deletedAt = null")
    Optional<Course> findByCourseIdAndUserId(@Param("courseId") Long courseId,  @Param("userId") Long userId);

    // DELETE
}
