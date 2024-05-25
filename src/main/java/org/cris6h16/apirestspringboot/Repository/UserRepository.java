package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNullApi;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

//public interface UserRepository extends
//        CrudRepository<UserEntity, Long>,
//        PagingAndSortingRepository<UserEntity, Long> {

public interface UserRepository extends
        JpaRepository<UserEntity, Long>,
        PagingAndSortingRepository<UserEntity, Long> {


    Optional<UserEntity> findByUsername(String username); // --> UserDetailServiceImpl |--| check if already exists -> @Service

    Optional<UserEntity> findByEmail(String email);
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class)
    default void executeInTransaction(Runnable runnable) {
            Objects.requireNonNull(runnable);
            runnable.run();
    }
}
