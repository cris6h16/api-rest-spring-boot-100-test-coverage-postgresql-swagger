package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;
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
    /**
     * Find a {@link UserEntity} by its {@code username}
     *
     * @param username of the {@link UserEntity} to retrieve
     * @return an {@link Optional} of {@link UserEntity} if found, {@link Optional#empty()} otherwise
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    Optional<UserEntity> findByUsername(String username);


    @Modifying
    @Query("UPDATE UserEntity u SET u.email = :newEmail WHERE u.id = :userId")
    void updateEmailById(String newEmail, Long userId);

    @Modifying
    @Query("UPDATE UserEntity u SET u.username = :newUsername WHERE u.id = :userId")
    void updateUsernameById(String newUsername, Long userId);

    @Modifying
    @Query("UPDATE UserEntity u SET u.password = :newPassword WHERE u.id = :id")
    void updatePasswordById(String newPassword, Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
