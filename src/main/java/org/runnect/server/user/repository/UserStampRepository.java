package org.runnect.server.user.repository;

import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.UserStamp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStampRepository extends JpaRepository<UserStamp, Long> {

    // CREATE
    UserStamp save(UserStamp userStamp);

    // READ
    long countByRunnectUser(RunnectUser runnectUser);

    // DELETE

}
