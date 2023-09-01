package org.runnect.server.publicCourse.repository;

import org.runnect.server.course.entity.Course;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublicCourseRepository  extends JpaRepository<PublicCourse, Long> {
    // CREATE

    // READ
    Optional<PublicCourse> findById(Long publicCourseId);

    // DELETE
}
