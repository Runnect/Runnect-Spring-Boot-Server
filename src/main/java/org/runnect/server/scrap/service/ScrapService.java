package org.runnect.server.scrap.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.NotFoundException;
import org.runnect.server.publicCourse.entity.PublicCourse;
import org.runnect.server.publicCourse.repository.PublicCourseRepository;
import org.runnect.server.scrap.dto.request.CreateAndDeleteScrapRequestDto;
import org.runnect.server.scrap.dto.response.DepartureResponse;
import org.runnect.server.scrap.dto.response.GetScrapCourseResponseDto;
import org.runnect.server.scrap.dto.response.ScrapResponse;
import org.runnect.server.scrap.dto.response.UserResponse;
import org.runnect.server.scrap.entity.Scrap;
import org.runnect.server.scrap.repository.ScrapRepository;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.StampType;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.runnect.server.user.service.UserStampService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;
    private final PublicCourseRepository publicCourseRepository;
    private final UserStampService userStampService;

    @Transactional
    public void createAndDeleteScrap(Long userId, CreateAndDeleteScrapRequestDto request) {
        Scrap scrap = scrapRepository.findByUserIdAndPublicCourseId(userId, request.getPublicCourseId()).orElse(null);
        RunnectUser user = userRepository.findById(userId).orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION, ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));
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

                user.updateCreatedScrap();
                userStampService.createStampByUser(user, StampType.S);

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

    public GetScrapCourseResponseDto getScrapCourseByUser(Long userId) {
        List<Scrap> scraps = scrapRepository.findAllByUserIdAndScrapTF(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_SCRAP_EXCEPTION, ErrorStatus.NOT_FOUND_SCRAP_EXCEPTION.getMessage()));

        List<ScrapResponse> scrapResponses = scraps.stream()
                .map(scrap -> ScrapResponse.of(
                        scrap.getId(),
                        scrap.getPublicCourse().getId(),
                        scrap.getPublicCourse().getCourse().getId(),
                        scrap.getPublicCourse().getTitle(),
                        scrap.getPublicCourse().getCourse().getImage(),
                        DepartureResponse.of(
                                scrap.getPublicCourse().getCourse().getDepartureRegion(),
                                scrap.getPublicCourse().getCourse().getDepartureCity())
                )).collect(Collectors.toList());

        return GetScrapCourseResponseDto.of(UserResponse.of(userId), scrapResponses);
    }
}
