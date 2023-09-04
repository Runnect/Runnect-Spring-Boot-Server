package org.runnect.server.scrap.service;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.exception.ErrorStatus;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.publicCourse.repository.PublicCourseRepository;
import org.runnect.server.scrap.dto.request.CreateAndDeleteScrapRequestDto;
import org.runnect.server.scrap.entity.Scrap;
import org.runnect.server.scrap.repository.ScrapRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScrapService {
    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;
    private final PublicCourseRepository publicCourseRepository;
    @Transactional
    public void createAndDeleteScrap(Long userId, CreateAndDeleteScrapRequestDto request) {
        Scrap scrap = scrapRepository.findByUserIdAndPublicCourseId(userId, request.getPublicCourseId()).orElse(null);
        RunnectUser user = userRepository.findById(userId).orElse(null);
        // 스크랩 생성
        if (request.getScrapTF() == true) {
            if (scrap == null) {
                // 기존 스크랩한 내역이 없을 때
                PublicCourse publicCourse = publicCourseRepository.findById(request.getPublicCourseId())
                        .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_PUBLICCOURSE_EXCEPTION, ErrorStatus.NOT_FOUND_PUBLICCOURSE_EXCEPTION.getMessage()));
                Scrap newScrap = Scrap.builder()
                        .scrapTF(true)
                        .publicCourse(publicCourse)
                        .runnectUser(user)
                        .build();
                scrapRepository.save(newScrap);
            } else {
                // 기존 스크랩한 내역이 있을 때
                scrap.updateScrapTF(true);
            }
        }
        // 스크랩 삭제
        else {
            scrap.updateScrapTF(false);
        }
    }
}
