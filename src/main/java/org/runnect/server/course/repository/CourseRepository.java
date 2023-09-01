package org.runnect.server.course.repository;


import org.runnect.server.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // CREATE
    Course save(Course course);

    // READ
    Course getCourseById(Long courseId);

    // DELETE
}
