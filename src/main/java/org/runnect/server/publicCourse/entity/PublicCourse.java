package org.runnect.server.publicCourse.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.entity.AuditingTimeEntity;
import org.runnect.server.course.entity.Course;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PublicCourse extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 40)
    private String title;

    @Column(nullable = false)
    private String description;

    @OneToMany(mappedBy = "public_course", cascade = CascadeType.REMOVE)
    private List<PublicCourse> publicCourses = new ArrayList<>();

    @Builder
    public PublicCourse(Course course, String title, String description) {
        this.course = course;
        this.title = title;
        this.description = description;
    }

}
