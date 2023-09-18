package org.runnect.server.user.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.common.dto.DepartureResponse;
import org.runnect.server.course.entity.Course;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.user.entity.RunnectUser;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfileResponseDto {

    private UserProfile user;
    private List<PublicCourseResponse> courses;

    public static UserProfileResponseDto of(UserProfile user, List<PublicCourseResponse> courses) {
        return new UserProfileResponseDto(user, courses);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserProfile {

        private Long userId;
        private String nickname;
        private String latestStamp;
        private int level;
        private int levelPercent;

        public static UserProfile of(RunnectUser user, int levelPercent) {
            return new UserProfile(user.getId(), user.getNickname(),
                user.getLatestStamp().toString(), user.getLevel(), levelPercent);
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PublicCourseResponse {

        private Long publicCourseId;
        private Long courseId;
        private String title;
        private String image;
        private DepartureResponse departure;
        private Boolean scrapTF;

        public static PublicCourseResponse of(PublicCourse publicCourse, Course course,
            Boolean scrapTF) {
            return new PublicCourseResponse(
                publicCourse.getId(),
                course.getId(),
                publicCourse.getTitle(),
                course.getImage(),
                DepartureResponse.from(course),
                scrapTF
            );
        }
    }
}
