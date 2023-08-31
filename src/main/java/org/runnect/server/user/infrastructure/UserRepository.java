package org.runnect.server.user.infrastructure;

import org.runnect.server.user.entity.RunnectUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository  extends JpaRepository<RunnectUser, Long> {
    Optional<RunnectUser> findById(Long userId);
}
