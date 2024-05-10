package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public interface UserRepository extends
        CrudRepository<UserEntity, Long>,
        PagingAndSortingRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username); // --> UserDetailServiceImpl |--| check if already exists -> @Service

//    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.notes n WHERE u.username = ?1")
//    Optional<UserEntity> findByUsernameEagerly(String username);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.notes n WHERE u.id = ?1")
    Optional<UserEntity> findByIdEagerly(Long id);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.notes n")
    Collection<UserEntity> findAllEager();


    Optional<UserEntity> findByEmail(String email);


//    @Query("SELECT n FROM UserEntity u JOIN u.notes n WHERE u.id = ?1")
//    Collection<Object> findNotesByUserId(Long userId);

    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class)
    default boolean executeInTransaction(Runnable runnable) {
        Objects.requireNonNull(runnable);
        runnable.run();
        return true;
    }
}
