package org.runnect.server.user.service;

import lombok.RequiredArgsConstructor;
import org.runnect.server.common.exception.ErrorStatus;
import org.runnect.server.user.dto.response.MyPageResponseDto;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.exception.userException.NotFoundUserException;
import org.runnect.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MyPageResponseDto getMyPage(Long userId) {
        RunnectUser user = userRepository.findUserByIdWithUserStamps(userId)
            .orElseThrow(() -> new NotFoundUserException(ErrorStatus.NOT_FOUND_USER_EXCEPTION,
                ErrorStatus.NOT_FOUND_USER_EXCEPTION.getMessage()));

        return MyPageResponseDto.of(user, calculateUserLevelPercent(user));
    }

    private int calculateUserLevelPercent(RunnectUser user) {
        return (user.getUserStamps().size() % 4) * 25;
    }
}
