package org.runnect.server.user.repository;

import java.util.Optional;
import org.runnect.server.user.entity.RunnectUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface UserRepository extends Repository<RunnectUser, Long> {
    // CREATE

    // READ
    Optional<RunnectUser> findById(Long id);

    @Query("select u from RunnectUser u join fetch u.userStamps where u.id = :userId")
    Optional<RunnectUser> findUserByIdWithUserStamps(Long userId);

    // DELETE
}
