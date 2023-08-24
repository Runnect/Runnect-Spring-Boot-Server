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
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"email", "provider"}
        )
})
public class User extends AuditingTimeEntity {

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

    @ColumnDefault("CSPR0")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StampType latestStamp;

    @ColumnDefault("1")
    @Column(nullable = false)
    private int level;

    @Column
    private String refreshToken;

    @ColumnDefault("0")
    @Column(nullable = false)
    private Long createdCourse;

    @ColumnDefault("0")
    @Column(nullable = false)
    private Long createdRecord;

    @ColumnDefault("0")
    @Column(nullable = false)
    private Long createdPublicCourse;

    @ColumnDefault("0")
    @Column(nullable = false)
    private Long createdScrap;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Record> records = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Scrap> scraps = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<UserStamp> userStamps = new ArrayList<>();

    @Builder
    public User(String nickname, String socialId, String email, SocialType provider, String refreshToken) {
        this.nickname = nickname;
        this.socialId = socialId;
        this.email = email;
        this.provider = provider;
        this.refreshToken = refreshToken;
    }

}
