package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNullApi;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public interface UserRepository extends
        CrudRepository<UserEntity, Long>,
        PagingAndSortingRepository<UserEntity, Long> {


    Optional<UserEntity> findByUsername(String username); // --> UserDetailServiceImpl |--| check if already exists -> @Service

    Optional<UserEntity> findByEmail(String email);

//    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.notes n WHERE u.id = ?1")
//    @Query("SELECT n FROM UserEntity u JOIN u.notes n WHERE u.id = ?1")

    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class)
    default boolean executeInTransaction(Runnable runnable) {
        try {
            Objects.requireNonNull(runnable);
            runnable.run();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
