package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository extends CrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.notes n WHERE u.username = ?1")
    Optional<UserEntity> findByUsernameEagerly(String username);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.notes n WHERE u.id = ?1")
    Optional<UserEntity> findByIdEagerly(Long id);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.notes n")
    Collection<UserEntity> findAllEager();


    Optional<UserEntity> findByEmail(String email);
}
