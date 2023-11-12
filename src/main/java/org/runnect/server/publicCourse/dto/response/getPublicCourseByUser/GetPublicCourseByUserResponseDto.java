package org.runnect.server.publicCourse.dto.response.getPublicCourseByUser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetPublicCourseByUserResponseDto {
    private GetPublicCourseByUserUser user;
    private List<GetPublicCourseByUserPublicCourse> publicCourses;

    public static GetPublicCourseByUserResponseDto of(Long userId,
                                                      List<GetPublicCourseByUserPublicCourse> publicCourses ) {
        return new GetPublicCourseByUserResponseDto(GetPublicCourseByUserUser.from(userId), publicCourses);

    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class GetPublicCourseByUserUser {
        private Long id;

        public static GetPublicCourseByUserUser from(Long id) {
            return new GetPublicCourseByUserUser(id);
        }
    }
}

