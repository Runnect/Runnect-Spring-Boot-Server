package org.runnect.server.publicCourse.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.ConflictException;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.common.exception.PermissionDeniedException;
import org.runnect.server.course.dto.response.GetCourseDetailResponseDto;
import org.runnect.server.course.entity.Course;
import org.runnect.server.course.repository.CourseRepository;
import org.runnect.server.publicCourse.dto.request.CreatePublicCourseRequestDto;
import org.runnect.server.publicCourse.dto.request.DeletePublicCoursesRequestDto;
import org.runnect.server.publicCourse.dto.response.CreatePublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.DeletePublicCoursesResponseDto;
import org.runnect.server.publicCourse.dto.response.GetPublicCourseDetailResponseDto;
import org.runnect.server.publicCourse.dto.response.UpdatePublicCourseResponseDto;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.publicCourse.repository.PublicCourseRepository;
import org.runnect.server.record.entity.Record;
import org.runnect.server.scrap.repository.ScrapRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublicCourseService {

    private final PublicCourseRepository publicCourseRepository;
    private final UserRepository userRepository;
    private final ScrapRepository scrapRepository;
    private final CourseRepository courseRepository;

    public GetPublicCourseDetailResponseDto getPublicCourseDetail(final Long userId, final Long publicCourseId){
        //0. 유저가 존재하는지
        RunnectUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                        ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        //1. publicCourse가 존재하는지
        PublicCourse publicCourse = publicCourseRepository.findById(publicCourseId).orElseThrow(()->new NotFoundException(ErrorStatus.NOT_FOUND_PUBLIC_COURSE_EXCEPTION,
                ErrorStatus.NOT_FOUND_PUBLIC_COURSE_EXCEPTION.getMessage()));

        //2. 이미 삭제된 코스인지
        if(publicCourse.getCourse().getDeletedAt()==null){
            new NotFoundException(ErrorStatus.NOT_FOUND_PUBLIC_COURSE_EXCEPTION,
                    ErrorStatus.NOT_FOUND_PUBLIC_COURSE_EXCEPTION.getMessage());
        }

        return GetPublicCourseDetailResponseDto.of(user.getNickname(),user.getLevel(),user.getLatestStamp().name(),publicCourse.getCourse().getRunnectUser().equals(user),
                publicCourse.getId(), publicCourse.getCourse().getId(),)



    }

    @Transactional
    public CreatePublicCourseResponseDto createPublicCourse(
            final Long userId,
            final CreatePublicCourseRequestDto createPublicCourseRequestDto) {
        //1. 받은 userId가 유저가 존재하는지 확인
        RunnectUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                        ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));


        //2. 받은 coursId로 해당 코스가 존재하는지 확인
        Course course = courseRepository.findById(createPublicCourseRequestDto.getCourseId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_COURSE_EXCEPTION,
                        ErrorStatus.NOT_FOUND_COURSE_EXCEPTION.getMessage()));

        //3. user가 코스를 그린사람인지 확인
        if (!course.isMatchedUser(user)) {
            throw new PermissionDeniedException(ErrorStatus.UNMATCHED_COURSE_EXCEPTION,
                    ErrorStatus.UNMATCHED_COURSE_EXCEPTION.getMessage());
        }

        //4. 이미 업로드된 코스인지 확인
        if (!course.getIsPrivate()) {
            throw new ConflictException(ErrorStatus.ALREADY_UPLOAD_COURSE_EXCEPTION,
                    ErrorStatus.ALREADY_UPLOAD_COURSE_EXCEPTION.getMessage());
        }

        //5. pulblicCourse를 생성후 저장
        PublicCourse publicCourse = PublicCourse.builder()
                .course(course)
                .user(user)
                .title(createPublicCourseRequestDto.getTitle())
                .description(createPublicCourseRequestDto.getDescription())
                .build();
        publicCourseRepository.save(publicCourse);

        //6. course의 private을 false로 변경
        course.uploadCourse();

        //7. publicCourse와 만든 날짜를 response
        return CreatePublicCourseResponseDto.of(
                publicCourse.getId(),publicCourse.getCreatedAt().toString());

    }

    @Transactional
    public DeletePublicCoursesResponseDto deletePublicCourses(
            Long userId,
            DeletePublicCoursesRequestDto requestDto
    ) {
        RunnectUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                        ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        List<PublicCourse> publicCourses = publicCourseRepository.findByIdIn(
                requestDto.getPublicCourseIdList());


        if (publicCourses.size() != requestDto.getPublicCourseIdList().size()) {
            throw new NotFoundException(ErrorStatus.NOT_FOUND_PUBLICCOURSE_EXCEPTION, ErrorStatus.NOT_FOUND_PUBLICCOURSE_EXCEPTION.getMessage());
        }

        publicCourses.stream()
                .filter(pc -> !pc.getRunnectUser().equals(user))
                .findAny()
                .ifPresent(pc -> {
                    throw new PermissionDeniedException(
                            ErrorStatus.PERMISSION_DENIED_PUBLIC_COURSE_DELETE_EXCEPTION,
                            ErrorStatus.PERMISSION_DENIED_PUBLIC_COURSE_DELETE_EXCEPTION.getMessage());
                });


        scrapRepository.deleteByPublicCourseIn(publicCourses);
        publicCourses.forEach(publicCourse -> publicCourse.getRecords().forEach(Record::setPublicCourseNull));
        publicCourses.forEach(PublicCourse::updateDeletedAt);

        return DeletePublicCoursesResponseDto.from(publicCourses.size());
    }

    @Transactional
    public UpdatePublicCourseResponseDto updatePublicCourse(Long userId, Long publicCourseId, String title, String description) {
        PublicCourse publicCourse = publicCourseRepository.findById(publicCourseId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_PUBLIC_COURSE_EXCEPTION, ErrorStatus.NOT_FOUND_PUBLIC_COURSE_EXCEPTION.getMessage()));

        publicCourse.updatePublicCourse(title, description);

        return UpdatePublicCourseResponseDto.of(publicCourse);
    }
}
