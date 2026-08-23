package org.runnect.server.user.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.runnect.server.auth.service.AppleSignInService;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.UnauthorizedException;
import org.runnect.server.course.repository.CourseRepository;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.scrap.repository.ScrapRepository;
import org.runnect.server.user.dto.request.UpdateUserNicknameRequestDto;
import org.runnect.server.user.dto.response.DeleteUserResponseDto;
import org.runnect.server.user.dto.response.MyPageResponseDto;
import org.runnect.server.user.dto.response.UpdateUserNicknameResponseDto;
import org.runnect.server.user.dto.response.UserProfileResponseDto;
import org.runnect.server.user.dto.response.UserProfileResponseDto.PublicCourseResponse;
import org.runnect.server.user.dto.response.UserProfileResponseDto.UserProfile;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.runnect.server.user.exception.userException.DuplicateNicknameException;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ScrapRepository scrapRepository;
    private final AppleSignInService appleSignInService;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public MyPageResponseDto getMyPage(Long userId) {
        RunnectUser user = userRepository.findUserByIdWithUserStamps(userId)
            .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        return MyPageResponseDto.of(user, calculateUserLevelPercent(user));
    }

    @Transactional
    public UpdateUserNicknameResponseDto updateUserNickname(
        Long userId, UpdateUserNicknameRequestDto updateUserNicknameRequestDto
    ) {
        RunnectUser user = userRepository.findUserByIdWithUserStamps(userId)
            .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        String newNickname = updateUserNicknameRequestDto.getNickname();
        // 기존 닉네임과 동일한 값으로 "변경"하는 경우까지 중복으로 처리하면
        // 본인의 현재 닉네임을 다시 저장할 수 없게 되므로 그 경우는 제외한다.
        if (!user.getNickname().equals(newNickname) && userRepository.existsByNickname(newNickname)) {
            throw new DuplicateNicknameException(ErrorStatus.ALREADY_EXIST_NICKNAME_EXCEPTION, ErrorStatus.ALREADY_EXIST_NICKNAME_EXCEPTION.getMessage());
        }

        user.updateUserNickname(newNickname);

        // 서로 다른 두 유저가 동시에 같은 새 닉네임으로 변경을 요청하면 위의 existsByNickname
        // 조회에서 둘 다 "중복 없음"을 보고 여기까지 도달할 수 있다. 변경 자체는 더티 체킹이라
        // 여기서 바로 flush하지 않으면 unique 제약 위반이 트랜잭션 커밋 시점에야 터져 이 메서드
        // 밖에서 DataIntegrityViolationException으로 그대로 샌다. saveAndFlush로 여기서 직접
        // 터뜨려 잡고, 이미 존재하는 다른 예외(DuplicateNicknameException)로 변환한다.
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateNicknameException(ErrorStatus.ALREADY_EXIST_NICKNAME_EXCEPTION, ErrorStatus.ALREADY_EXIST_NICKNAME_EXCEPTION.getMessage());
        }

        return UpdateUserNicknameResponseDto.of(user, calculateUserLevelPercent(user));
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

        List<Long> scrappedPublicCourseIds = scrapRepository.getScrappedTruePublicCourseIds(requestUser);

        List<PublicCourse> publicCourses = courseRepository.findCoursesForUserProfile(profileUser)
                .stream().map(course -> course.getPublicCourse()).collect(Collectors.toList());

        List<PublicCourseResponse> publicCourseResponses = publicCourses.stream()
            .map(publicCourse ->
                PublicCourseResponse.of(publicCourse, publicCourse.getCourse(),
                    scrappedPublicCourseIds.contains(publicCourse.getId())))
            .collect(Collectors.toList());

        return UserProfileResponseDto.of(userProfile, publicCourseResponses);
    }



    @Transactional
    public DeleteUserResponseDto deleteUser(Long userId,String appleAccessToken){

        //1. 받은 userId가 유저가 존재하는지 확인
        RunnectUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                        ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        //2. 애플유저인데 accessToken이 없는 경우 에러
        if(user.getProvider().equals(SocialType.APPLE) && appleAccessToken==null){
            throw new UnauthorizedException(ErrorStatus.NOT_FOUND_APPLE_ACCESS_TOKEN,
                    ErrorStatus.NOT_FOUND_APPLE_ACCESS_TOKEN.getMessage());
        }

        //3. 애플유저인 경우 애플에 알리기
        if(user.getProvider().equals(SocialType.APPLE) && appleAccessToken!=null){
            appleSignInService.reportWithdrawalToApple(appleAccessToken);
        }

        //4. 유저삭제
        userRepository.delete(user);

        return DeleteUserResponseDto.of(userId);

    }


    private int calculateUserLevelPercent(RunnectUser user) {
        return (user.getUserStamps().size() % 4) * 25;
    }


}
