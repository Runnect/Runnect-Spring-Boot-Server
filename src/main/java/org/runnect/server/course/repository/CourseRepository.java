package org.runnect.server.course.repository;


import java.util.List;
import org.runnect.server.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // CREATE
    Course save(Course course);

    // READ
    @Query("select c from Course c join fetch c.runnectUser where c.runnectUser.id = :userId order by c.createdAt desc")
    List<Course> findCourseByUserId(Long userId);

    @Query("select c from Course c join fetch c.runnectUser where c.runnectUser.id = :userId and c.isPrivate = true order by c.createdAt desc")
    List<Course> findCourseByUserIdOnlyPrivate(Long userId);

    // DELETE
}
