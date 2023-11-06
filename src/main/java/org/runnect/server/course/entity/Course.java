package org.runnect.server.course.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.LineString;
import org.runnect.server.common.entity.AuditingTimeEntity;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.record.entity.Record;
import org.runnect.server.user.entity.RunnectUser;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private RunnectUser runnectUser;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 10)
    private String departureRegion;

    @Column(nullable = false, length = 10)
    private String departureCity;

    @Column(nullable = false, length = 10)
    private String departureTown;

    @Column
    private String departureDetail;

    @Column
    private String departureName;

    @Column(nullable = false)
    private Float distance;

    @Column(nullable = false)
    private String image;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate;

    @Column(nullable = false, columnDefinition = "geometry(LineString, 4326)")
    private LineString path;

    @OneToOne(mappedBy = "course")
    private PublicCourse publicCourse;

    @OneToMany(mappedBy = "course")
    private List<Record> records = new ArrayList<>();

    @Builder
    public Course(RunnectUser runnectUser, String title, String departureRegion, String departureCity, String departureTown, String departureDetail, String departureName, Float distance, String image, LineString path) {
        this.runnectUser = runnectUser;
        this.title = title;
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

    public void updateCourse(String title) {
        this.title = title;
    }

}
