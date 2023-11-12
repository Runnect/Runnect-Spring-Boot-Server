package org.runnect.server.user.repository;

import java.util.Optional;
import org.runnect.server.user.entity.RunnectUser;
import org.runnect.server.user.entity.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<RunnectUser, Long> {
    // CREATE

    // READ
    Optional<RunnectUser> findById(Long id);

    @Query("select u from RunnectUser u join fetch u.userStamps where u.id = :userId")
    Optional<RunnectUser> findUserByIdWithUserStamps(@Param("userId") Long userId);

    boolean existsByNickname(String nickname);

    boolean existsByEmailAndProvider(String email, SocialType provider);

    Optional<RunnectUser> findByEmailAndProvider(String email, SocialType provider);

    @Query("select u from RunnectUser u join fetch u.userStamps where u.id = :userId")
    Optional<RunnectUser> findByIdWithUserStamps(@Param("userId") Long userId);

    // DELETE

}
