package org.cris6h16.apirestspringboot.Repositories;

import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

/**
 * Repository for {@link UserEntity}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public interface UserRepository extends
        JpaRepository<UserEntity, Long>,
        PagingAndSortingRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserEntity u SET u.email = :newEmail, u.updatedAt = CURRENT DATE WHERE u.id = :userId")
    void updateEmailById(String newEmail, Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserEntity u SET u.username = :newUsername, u.updatedAt = CURRENT DATE WHERE u.id = :userId")
    void updateUsernameById(String newUsername, Long userId);

    @Modifying(clearAutomatically = true) // clearAutomatically = true, to avoid the `EntityManager` to be out of sync
    @Query("UPDATE UserEntity u SET u.password = :newPassword, u.updatedAt = CURRENT DATE WHERE u.id = :id")
    void updatePasswordById(String newPassword, Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
