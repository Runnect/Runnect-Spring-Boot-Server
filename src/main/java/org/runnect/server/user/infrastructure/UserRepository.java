package org.runnect.server.user.infrastructure;


import org.runnect.server.user.entity.RunnectUser;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface UserRepository extends Repository<RunnectUser, Long> {
    // CREATE

    // READ
    Optional<RunnectUser> findById(Long id);

    // DELETE
}