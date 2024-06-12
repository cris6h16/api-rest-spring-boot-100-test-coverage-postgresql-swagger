package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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


    /**
     * Find a {@link UserEntity} by its {@code email}
     *
     * @param email of the {@link UserEntity} to retrieve
     * @return an {@link Optional} of {@link UserEntity} if found, {@link Optional#empty()} otherwise
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    Optional<UserEntity> findByEmail(String email);


    /**
     * Execute a {@link Runnable} in a transaction,
     * <p>
     * we can use for testing purposes as:
     * save a {@link UserEntity} directly just using the
     * {@link UserRepository} without depends on
     * start a transaction first.
     * </p>
     *
     * @param runnable to execute in a transaction
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    default void executeInTransaction(Runnable runnable) {
        Objects.requireNonNull(runnable);
        runnable.run();
    }
}
