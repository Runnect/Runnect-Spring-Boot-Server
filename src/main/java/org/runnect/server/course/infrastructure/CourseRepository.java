package org.runnect.server.course.infrastructure;

import org.runnect.server.course.entity.Course;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface CourseRepository extends Repository<Course, Long> {
    Optional<Course> findById(Long courseId);
}
