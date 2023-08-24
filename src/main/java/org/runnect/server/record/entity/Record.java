package org.runnect.server.record.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.entity.AuditingTimeEntity;
import org.runnect.server.course.entity.Course;
import org.runnect.server.user.entity.User;

import javax.persistence.*;
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
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 40)
    private String title;

    @Column(nullable = false)
    private LocalTime pace;

    @Column(nullable = false)
    private LocalTime time;

    @Builder
    public Record(User user, Course course, String title, LocalTime pace, LocalTime time) {
        this.user = user;
        this.course = course;
        this.title = title;
        this.pace = pace;
        this.time = time;
    }

}
