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

    public RunnectUser(String nickname) {
        this.id = -1L;
        this.nickname = nickname;
        this.socialId = null;
        this.email = null;
        this.provider = null;
        this.latestStamp = StampType.DELETED_USER;
        this.level = -1;
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

    // 기본 equals(참조 비교)에 의존하면, 같은 유저를 서로 다른 조회 경로로 가져왔을 때
    // (예: 로그인 유저 vs 코스에 매핑된 유저) 같은 사람인데도 다르다고 판정될 수 있다.
    // id 기준으로 비교하고, hashCode는 영속화 전후로 값이 바뀌지 않도록 상수로 고정한다.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RunnectUser)) {
            return false;
        }
        RunnectUser that = (RunnectUser) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
