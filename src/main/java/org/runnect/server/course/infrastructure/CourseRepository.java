package org.runnect.server.course.infrastructure;

import org.runnect.server.course.entity.Course;

public interface CourseRepository {
    Course findById(Long courseId);
}
