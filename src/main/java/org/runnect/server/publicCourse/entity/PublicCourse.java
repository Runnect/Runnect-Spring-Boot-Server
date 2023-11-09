package org.runnect.server.publicCourse.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.entity.AuditingTimeEntity;
import org.runnect.server.course.entity.Course;
import org.runnect.server.record.entity.Record;
import org.runnect.server.scrap.entity.Scrap;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import org.runnect.server.user.entity.RunnectUser;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PublicCourse extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 40)
    private String title;

    @Column(nullable = false)
    private String description;

    // 아래 코드 주석이유 -> 유저가 현재 스크랩한 코스는 scraptf가 treu인 경우만임
//    @OneToMany(mappedBy = "publicCourse")
//    private List<Scrap> scraps = new ArrayList<>();

    @OneToMany(mappedBy = "publicCourse")
    private List<Record> records = new ArrayList<>();

    @Transient
    private Boolean isScrap=false; //현재 사용자가 스크랩한지 아닌지 여부

    public void setIsScrap(Boolean flag){ isScrap=flag;}

    @Builder
    public PublicCourse(Course course, String title, String description) {
        this.course = course;
        this.title = title;
        this.description = description;
    }

    public void updatePublicCourse(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
