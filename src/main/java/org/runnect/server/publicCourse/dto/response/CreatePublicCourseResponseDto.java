package org.runnect.server.publicCourse.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.runnect.server.publicCourse.entity.PublicCourse;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreatePublicCourseResponseDto {

    private CreatePublicCourseResponseDto.CreateCourseResponse publicCourse;

    public static CreatePublicCourseResponseDto of(Long id, String createdAt){
        return new CreatePublicCourseResponseDto(CreatePublicCourseResponseDto.CreateCourseResponse.from(id, createdAt));
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CreateCourseResponse{
        private Long id;
        private String createdAt;

        public static CreatePublicCourseResponseDto.CreateCourseResponse from(Long id, String createdAt){
            return new CreatePublicCourseResponseDto.CreateCourseResponse(id,createdAt);
        }
    }



}
