package org.runnect.server.publicCourse.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.exception.ErrorStatus;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.common.exception.PermissionDeniedException;
import org.runnect.server.publicCourse.dto.request.DeletePublicCoursesRequestDto;
import org.runnect.server.publicCourse.dto.request.UpdatePublicCourseRequestDto;
import org.runnect.server.publicCourse.dto.response.DeletePublicCoursesResponseDto;
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
                .orElseThrow(()->new NotFoundException(ErrorStatus.NOT_FOUND_PUBLIC_COURSE_EXCEPTION, ErrorStatus.NOT_FOUND_PUBLIC_COURSE_EXCEPTION.getMessage()));

        publicCourse.updatePublicCourse(title, description);

        return UpdatePublicCourseResponseDto.of(publicCourse);
    }
}
