package org.runnect.server.user.service;

import static org.runnect.server.common.constant.ErrorStatus.NOT_FOUND_USER_EXCEPTION;

import lombok.RequiredArgsConstructor;
import org.runnect.server.user.dto.response.GetUserStampsResponseDto;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.StampType;
import org.runnect.server.user.entity.UserStamp;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.runnect.server.user.repository.UserStampRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStampService {

    private final UserStampRepository userStampRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createStampByUser(RunnectUser user, StampType stampType) {
        Long activityCount = getActivityCount(user, stampType);
        if (activityCount == 0) {
            return;
        }

        StampType newStamp = checkStampQualificationAndType(stampType, activityCount);
        if (newStamp == null) {
            return;
        }

        createUserStamp(user, newStamp);
        userLevelUpdate(user);
    }

    @Transactional(readOnly = true)
    public GetUserStampsResponseDto findUserStamps(Long userId) {
        return GetUserStampsResponseDto.from(userRepository.findUserByIdWithUserStamps(userId)
            .orElseThrow(() -> new NotFoundUserException(NOT_FOUND_USER_EXCEPTION,
                NOT_FOUND_USER_EXCEPTION.getMessage())));
    }

    private void createUserStamp(RunnectUser user, StampType stampType) {
        UserStamp userStamp = UserStamp.builder()
            .stampId(stampType)
            .runnectUser(user)
            .build();

        userStampRepository.save(userStamp);
    }

    private void userLevelUpdate(RunnectUser user) {
        Long stampCount = userStampRepository.countByRunnectUser(user);
        if (stampCount % 4 == 0 && stampCount <= 12) {
            user.updateUserLevel(stampCount.intValue() / 4 + 1);
        }
    }

    // 스탬프 추가할지 말지, 한다면 어떤 스탬프를 줘야하는지 check
    private StampType checkStampQualificationAndType(StampType stampType, Long activityCount) {
        for (int i = 3; i >= 1; i--) {
            if (activityCount == StampType.getLevelUpCriteria(stampType, i)) {
                return StampType.getStampType(stampType, i);
            }
        }
        return null;
    }

    private Long getActivityCount(RunnectUser user, StampType stampType) {
        Long activityCount = 0L;
        switch (stampType) {
            case C:
                activityCount = user.getCreatedCourse();
                break;
            case S:
                activityCount = user.getCreatedScrap();
                break;
            case R:
                activityCount = user.getCreatedRecord();
                break;
            case U:
                activityCount = user.getCreatedPublicCourse();
                break;
        }
        return activityCount;
    }
}
