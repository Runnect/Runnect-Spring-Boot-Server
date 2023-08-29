package org.runnect.server.course.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.entity.AuditingTimeEntity;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.record.entity.Record;
import org.runnect.server.user.entity.RunnectUser;
import org.postgresql.geometric.PGpath;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private RunnectUser runnectUser;

    @Column(nullable = false, length = 10)
    private String departureRegion;

    @Column(nullable = false, length = 10)
    private String departureCity;

    @Column(nullable = false, length = 10)
    private String departureTown;

    @Column(nullable = false, length = 20)
    private String departureDetail;

    @Column(nullable = false)
    private String departureName;

    @Column(nullable = false)
    private Float distance;

    @Column(nullable = false)
    private String image;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate;

    @Column(nullable = false)
    private PGpath path;

    @OneToOne(mappedBy = "course", cascade = CascadeType.REMOVE)
    private PublicCourse publicCourse;

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE)
    private List<Record> records = new ArrayList<>();

    @Builder
    public Course(RunnectUser runnectUser, String departureRegion, String departureCity, String departureTown, String departureDetail, String departureName, Float distance, String image, PGpath path) {
        this.runnectUser = runnectUser;
        this.departureRegion = departureRegion;
        this.departureCity = departureCity;
        this.departureTown = departureTown;
        this.departureDetail = departureDetail;
        this.departureName = departureName;
        this.distance = distance;
        this.image = image;
        this.path = path;
        this.isPrivate = true;
    }

}
