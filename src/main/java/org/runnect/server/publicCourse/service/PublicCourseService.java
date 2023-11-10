package org.runnect.server.publicCourse.service;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.constant.SortStatus;
import org.runnect.server.common.exception.ConflictException;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.common.exception.PermissionDeniedException;
import org.runnect.server.course.entity.Course;
import org.runnect.server.course.repository.CourseRepository;
import org.runnect.server.publicCourse.dto.request.CreatePublicCourseRequestDto;
import org.runnect.server.publicCourse.dto.request.DeletePublicCoursesRequestDto;
import org.runnect.server.publicCourse.dto.response.CreatePublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.DeletePublicCoursesResponseDto;
import org.runnect.server.publicCourse.dto.response.UpdatePublicCourseResponseDto;
import org.runnect.server.publicCourse.dto.response.getPublicCourseByUser.GetPublicCourseByUserPublicCourse;
import org.runnect.server.publicCourse.dto.response.getPublicCourseByUser.GetPublicCourseByUserResponseDto;
import org.runnect.server.publicCourse.dto.response.recommendPublicCourse.RecommendPublicCourse;
import org.runnect.server.publicCourse.dto.response.recommendPublicCourse.RecommendPublicCourseResponseDto;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.publicCourse.repository.PublicCourseRepository;
import org.runnect.server.record.entity.Record;
import org.runnect.server.scrap.entity.Scrap;
import org.runnect.server.scrap.repository.ScrapRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublicCourseService {
    private static final Integer PAGE_SIZE = 10;

    private final PublicCourseRepository publicCourseRepository;
    private final UserRepository userRepository;
    private final ScrapRepository scrapRepository;
    private final CourseRepository courseRepository;


    public RecommendPublicCourseResponseDto recommendPublicCourse(Long userId, Integer pageNo, String sort){
        //1. 받은 userId가 유저가 존재하는지 확인
        RunnectUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                        ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        //2. 유저가 스크랩한 코스들 가져오기
        List<Scrap> scraps = scrapRepository.findAllByUserIdAndScrapTF(userId).get();


        //3. page, sort 에 따라 데이터 가져오기
        List<RecommendPublicCourse> recommendPublicCourses = new ArrayList<>();
        Page<PublicCourse> publicCourses = null;
        if(SortStatus.SCRAP_DESC.getVlaue().equals(sort)){
            publicCourses = publicCourseRepository.findAll(
                    PageRequest.of(pageNo-1, PAGE_SIZE,
                            Sort.by(Sort.Direction.DESC,SortStatus.SCRAP_DESC.getProperty())));

        } else if (SortStatus.DATE_DESC.getVlaue().equals(sort)) {
            publicCourses = publicCourseRepository.findAll(
                    PageRequest.of(pageNo-1, PAGE_SIZE,
                            Sort.by(Sort.Direction.DESC,SortStatus.DATE_DESC.getProperty())));
        }

        publicCourses.forEach(publicCourse->{
            //4. 각 코스들의 publicCourse와 scrap 여부 파악
            scraps.forEach(scrap->
                    publicCourse.setIsScrap(scrap.getPublicCourse().equals(publicCourse)));

            recommendPublicCourses.add(
                    RecommendPublicCourse.of(
                            publicCourse.getId(),
                            publicCourse.getCourse().getId(),
                            publicCourse.getTitle(),
                            publicCourse.getCourse().getImage(),
                            publicCourse.getIsScrap(),
                            publicCourse.getCourse().getDepartureRegion(),
                            publicCourse.getCourse().getDepartureCity()));
        });


        return RecommendPublicCourseResponseDto.of(sort,recommendPublicCourses);

    }

    public GetPublicCourseByUserResponseDto getPublicCourseByUser(Long userId){
        List<GetPublicCourseByUserPublicCourse> getPublicCourseByUserPublicCourses = new ArrayList<>();

        //1. 받은 userId가 유저가 존재하는지 확인
        RunnectUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                        ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        //2. userId에 연결된 course중 private이 false인 코스만 가져오기
        List<Course> courses = courseRepository.findCoursesByRunnectUserAndIsPrivateIsFalse(user);

        //3. 유저가 스크랩한 코스들 가져오기
        List<Scrap> scraps = scrapRepository.findAllByUserIdAndScrapTF(userId).get();

        courses.forEach(course->{
            PublicCourse publicCourse = course.getPublicCourse();

            //4. 각 코스들의 publicCourse와 scrap 여부 파악
            scraps.forEach(scrap->
                    publicCourse.setIsScrap(scrap.getPublicCourse().equals(publicCourse)));

            //5. responseDto 만듬
            getPublicCourseByUserPublicCourses.add(
                    GetPublicCourseByUserPublicCourse.of(
                            publicCourse.getId(),
                            course.getId(),
                            publicCourse.getIsScrap(),
                            publicCourse.getTitle(),
                            course.getImage(),
                            course.getDepartureRegion(),
                            course.getDepartureCity()));
        });



        return GetPublicCourseByUserResponseDto.of(userId, getPublicCourseByUserPublicCourses);
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
