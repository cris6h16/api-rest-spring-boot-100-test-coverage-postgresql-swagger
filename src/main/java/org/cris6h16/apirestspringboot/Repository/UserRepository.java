package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    int countByUsername(String username);

    int countByEmail(String email);
}
