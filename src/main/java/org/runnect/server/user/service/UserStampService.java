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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStampService {

    private final UserStampRepository userStampRepository;
    private final UserRepository userRepository;

    /**
     * 활동 카운터 증가 + 스탬프 지급을, 이 활동을 유발한 메인 트랜잭션(코스/기록/스크랩 생성)과
     * 분리된 별도 트랜잭션에서 수행한다. RunnectUser의 카운터 필드에는 낙관적 락(@Version)이
     * 걸려있어, 동시에 같은 유저의 카운터를 갱신하는 다른 요청과 충돌하면
     * ObjectOptimisticLockingFailureException이 발생한다 — 메인 트랜잭션(코스/기록/스크랩 생성
     * 자체)까지 롤백시키지 않기 위해 REQUIRES_NEW로 격리했다. 충돌 시 재시도는 호출부가
     * OptimisticLockRetrier로 감싸서 처리한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordActivityAndAwardStamp(Long userId, StampType stampType) {
        RunnectUser user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundUserException(NOT_FOUND_USER_EXCEPTION,
                NOT_FOUND_USER_EXCEPTION.getMessage()));

        incrementActivityCount(user, stampType);
        createStampByUser(user, stampType);

        // 여기서 명시적으로 flush해서, 버전 충돌이 이 메서드 안에서 즉시 드러나게 한다
        // (그래야 REQUIRES_NEW 트랜잭션 경계를 벗어나기 전에 예외가 호출부로 전파된다).
        userRepository.saveAndFlush(user);
    }

    private void incrementActivityCount(RunnectUser user, StampType stampType) {
        switch (stampType) {
            case c:
                user.updateCreatedCourse();
                break;
            case s:
                user.updateCreatedScrap();
                break;
            case r:
                user.updateCreatedRecord();
                break;
            case u:
                user.updateCreatedPublicCourse();
                break;
        }
    }

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
            case c:
                activityCount = user.getCreatedCourse();
                break;
            case s:
                activityCount = user.getCreatedScrap();
                break;
            case r:
                activityCount = user.getCreatedRecord();
                break;
            case u:
                activityCount = user.getCreatedPublicCourse();
                break;
        }
        return activityCount;
    }
}
