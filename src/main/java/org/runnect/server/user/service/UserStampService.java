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
        Long activityCount = getActivityCount(user, stampType);
        if (activityCount == 0) return;

        StampType newStamp = checkStampQualificationAndType(stampType, activityCount);
        if (newStamp == null) return;

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
                activityCount = publicCourseRepository.countPublicCourseByUser(user);
                break;
        }
        return activityCount;
    }
}
