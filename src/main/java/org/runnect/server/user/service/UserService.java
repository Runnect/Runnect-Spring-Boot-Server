package org.runnect.server.user.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.exception.ErrorStatus;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.publicCourse.repository.PublicCourseRepository;
import org.runnect.server.scrap.repository.ScrapRepository;
import org.runnect.server.user.dto.response.MyPageResponseDto;
import org.runnect.server.user.dto.response.UserProfileResponseDto;
import org.runnect.server.user.dto.response.UserProfileResponseDto.PublicCourseResponse;
import org.runnect.server.user.dto.response.UserProfileResponseDto.UserProfile;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PublicCourseRepository publicCourseRepository;
    private final ScrapRepository scrapRepository;

    @Transactional(readOnly = true)
    public MyPageResponseDto getMyPage(Long userId) {
        RunnectUser user = userRepository.findUserByIdWithUserStamps(userId)
            .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        return MyPageResponseDto.of(user, calculateUserLevelPercent(user));
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile(Long profileUserId, Long requestUserId) {
        RunnectUser profileUser = userRepository.findUserByIdWithUserStamps(profileUserId)
            .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));
        UserProfile userProfile = UserProfile.of(profileUser,
            calculateUserLevelPercent(profileUser));

        RunnectUser requestUser = userRepository.findById(requestUserId)
            .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        List<PublicCourse> publicCourses = publicCourseRepository.findPublicCoursesByRunnectUser(
            profileUser);

        List<PublicCourseResponse> publicCourseResponses = publicCourses.stream()
            .map(publicCourse ->
                PublicCourseResponse.of(publicCourse, publicCourse.getCourse(),
                    scrapRepository.existsByPublicCourseAndRunnectUser(publicCourse, requestUser)))
            .collect(Collectors.toList());

        return UserProfileResponseDto.of(userProfile, publicCourseResponses);
    }

    private int calculateUserLevelPercent(RunnectUser user) {
        return (user.getUserStamps().size() % 4) * 25;
    }
}
