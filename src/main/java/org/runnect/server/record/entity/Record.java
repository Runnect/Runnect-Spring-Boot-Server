package org.runnect.server.record.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.entity.AuditingTimeEntity;
import org.runnect.server.course.entity.Course;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.user.entity.RunnectUser;

import javax.persistence.*;
import java.sql.Time;
import java.time.LocalTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Record extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private RunnectUser runnectUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "public_course_id")
    private PublicCourse publicCourse;

    @Column(nullable = false, length = 40)
    private String title;

    @Column(nullable = false)
    private Time pace;

    @Column(nullable = false)
    private Time time;

    @Builder
    public Record(RunnectUser runnectUser, Course course, PublicCourse publicCourse, String title, Time pace, Time time) {
        this.runnectUser = runnectUser;
        this.course = course;
        this.publicCourse = publicCourse;
        this.title = title;
        this.pace = pace;
        this.time = time;
    }

}
