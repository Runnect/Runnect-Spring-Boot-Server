package org.runnect.server.user.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.runnect.server.common.entity.AuditingTimeEntity;
import org.runnect.server.course.entity.Course;
import org.runnect.server.record.entity.Record;
import org.runnect.server.scrap.entity.Scrap;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @Column
    private String refreshToken;

    @Column(nullable = false)
    private Long createdCourse;

    @Column(nullable = false)
    private Long createdRecord;

    @Column(nullable = false)
    private Long createdPublicCourse;

    @Column(nullable = false)
    private Long createdScrap;

    @OneToMany(mappedBy = "runnectUser", cascade = CascadeType.REMOVE)
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "runnectUser", cascade = CascadeType.REMOVE)
    private List<Record> records = new ArrayList<>();

    @OneToMany(mappedBy = "runnectUser", cascade = CascadeType.REMOVE)
    private List<Scrap> scraps = new ArrayList<>();

    @OneToMany(mappedBy = "runnectUser", cascade = CascadeType.REMOVE)
    private List<UserStamp> userStamps = new ArrayList<>();

    @Builder
    public RunnectUser(String nickname, String socialId, String email, SocialType provider, String refreshToken) {
        this.nickname = nickname;
        this.socialId = socialId;
        this.email = email;
        this.provider = provider;
        this.refreshToken = refreshToken;
        this.latestStamp = StampType.CSPR0;
        this.level = 1;
        this.createdCourse = 0L;
        this.createdRecord = 0L;
        this.createdPublicCourse = 0L;
        this.createdScrap = 0L;
    }

}
