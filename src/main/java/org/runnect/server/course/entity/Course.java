package org.runnect.server.course.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.entity.AuditingTimeEntity;
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

    @Column(nullable = false)
    private String path;

    @OneToMany(mappedBy = "course")
    private List<Record> records = new ArrayList<>();

    @Builder
    public Course(RunnectUser runnectUser, String title, String departureRegion, String departureCity, String departureTown, String departureDetail, String departureName, Float distance, String image, String path) {
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
