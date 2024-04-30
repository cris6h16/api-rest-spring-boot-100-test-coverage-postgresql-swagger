package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<RoleEntity, Long> {
}
