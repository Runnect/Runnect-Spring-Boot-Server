package org.runnect.server.scrap.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.entity.AuditingTimeEntity;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.user.entity.User;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"user_id", "public_course_id"}
        )
})
public class Scrap extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "public_course_id", nullable = false)
    private PublicCourse publicCourse;

    @Column(nullable = false)
    private Boolean scrapTF;

    @Builder
    public Scrap(User user, PublicCourse publicCourse, Boolean scrapTF) {
        this.user = user;
        this.publicCourse = publicCourse;
        this.scrapTF = scrapTF;
    }

}
