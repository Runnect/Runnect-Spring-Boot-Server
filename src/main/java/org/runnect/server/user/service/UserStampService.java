package org.runnect.server.user.service;

import lombok.RequiredArgsConstructor;
import org.runnect.server.course.repository.CourseRepository;
import org.runnect.server.publicCourse.repository.PublicCourseRepository;
import org.runnect.server.record.repository.RecordRepository;
import org.runnect.server.scrap.repository.ScrapRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.StampType;
import org.runnect.server.user.entity.UserStamp;
import org.runnect.server.user.repository.UserStampRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStampService {

    private final UserStampRepository userStampRepository;
    private final CourseRepository courseRepository;
    private final ScrapRepository scrapRepository;
    private final RecordRepository recordRepository;
    private final PublicCourseRepository publicCourseRepository;

    @Transactional
    public void createStampByUser(RunnectUser user, StampType stampType) {
        // 1. stampType에 해당하는 활동 횟수 가져옴! -> 예를 들면 C : 해당 유저가 그린 코스 횟수 가져옴
        Long activityCount = getActivityCount(user, stampType);
        // 2. 횟수 0이면 바로 return
        if (activityCount == 0) return;
        // 3. 스탬프 추가할지 말지, 한다면 어떤 스탬프인지 확인
        StampType newStamp = checkStampQualificationAndType(stampType, activityCount);
        if (newStamp == null) return;
        // 4. 스탬프 추가 & 추가 했으면 레벨업 체크
        createUserStamp(user, newStamp);
        userLevelUpdate(user);
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
                activityCount = courseRepository.countByRunnectUser(user);
                break;
            case S:
                activityCount = scrapRepository.countByRunnectUser(user);
                break;
            case R:
                activityCount = recordRepository.countByRunnectUser(user);
                break;
            case U:
                activityCount = publicCourseRepository.countPublicCOurseByUser(user);
                break;
        }
        return activityCount;
    }
}
