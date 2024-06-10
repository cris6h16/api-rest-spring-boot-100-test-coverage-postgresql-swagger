package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Repository for {@link RoleEntity}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    /**
     * Find a {@link RoleEntity} by its {@code name}
     *
     * @param name of the {@link RoleEntity}
     * @return {@link Optional} of {@link RoleEntity} if found, {@link Optional#empty()} otherwise
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    Optional<RoleEntity> findByName(ERole name);
}
