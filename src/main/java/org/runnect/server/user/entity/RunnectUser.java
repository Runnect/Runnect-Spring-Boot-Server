package org.runnect.server.user.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.entity.AuditingTimeEntity;
import org.runnect.server.course.entity.Course;
import org.runnect.server.record.entity.Record;
import org.runnect.server.scrap.entity.Scrap;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
    @UniqueConstraint(
        columnNames = {"email", "provider"}
    )
})
public class RunnectUser extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 14)
    private String nickname;

    @Column(nullable = false)
    private String socialId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SocialType provider;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StampType latestStamp;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private Long createdCourse;

    @Column(nullable = false)
    private Long createdRecord;

    @Column(nullable = false)
    private Long createdPublicCourse;

    @Column(nullable = false)
    private Long createdScrap;

    @OneToMany(mappedBy = "runnectUser")
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "runnectUser")
    private List<Record> records = new ArrayList<>();

    @OneToMany(mappedBy = "runnectUser")
    private List<Scrap> scraps = new ArrayList<>();

    @OneToMany(mappedBy = "runnectUser")
    private List<UserStamp> userStamps = new ArrayList<>();

    @Builder
    public RunnectUser(String nickname, String socialId, String email, SocialType provider) {
        this.nickname = nickname;
        this.socialId = socialId;
        this.email = email;
        this.provider = provider;
        this.latestStamp = StampType.CSPR0;
        this.level = 1;
        this.createdCourse = 0L;
        this.createdRecord = 0L;
        this.createdPublicCourse = 0L;
        this.createdScrap = 0L;
    }

    public void updateUserLevel(int level) {
        this.level = level;
    }

    public void updateUserNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateCreatedCourse() {
        this.createdCourse++;
    }

    public void updateCreatedRecord() {
        this.createdRecord++;
    }

    public void updateCreatedScrap() {
        this.createdScrap++;
    }

    public void updateCreatedPublicCourse() {
        this.createdPublicCourse++;
    }

    @Override
    public void updateDeletedAt() {
        throw new RuntimeException("Course를 제외한 테이블은 정상적으로 삭제됩니다.");
    }
}
