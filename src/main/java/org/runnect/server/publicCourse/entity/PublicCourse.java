package org.runnect.server.publicCourse.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;
import org.runnect.server.common.entity.AuditingTimeEntity;
import org.runnect.server.course.entity.Course;
import org.runnect.server.record.entity.Record;
import org.runnect.server.scrap.entity.Scrap;

import javax.persistence.*;
import java.time.LocalDateTime;
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


    @OneToMany(mappedBy = "publicCourse")
    private List<Record> records = new ArrayList<>();

    @Formula("(select count(*) from Scrap where Scrap.public_course_id=id and Scrap.scraptf=true)")
    private Long scrapCount;

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

    @Override
    public void updateDeletedAt() {
        throw new RuntimeException("Course를 제외한 테이블은 정상적으로 삭제됩니다.");
    }

    // RunnectUser와 동일한 이유(참조 동일성 의존 방지)로 id 기준 equals/hashCode를 둔다.
    // scrap 목록과 publicCourse 목록을 서로 다른 쿼리로 가져와 비교하는 곳(isScrap 매칭)이 많아서 영향이 크다.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PublicCourse)) {
            return false;
        }
        PublicCourse that = (PublicCourse) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
