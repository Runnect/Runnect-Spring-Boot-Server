package org.runnect.server.course.repository;


import org.runnect.server.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // CREATE
    Course save(Course course);

    // READ
    Optional<Course> findById(Long courseId);

    // DELETE
}
