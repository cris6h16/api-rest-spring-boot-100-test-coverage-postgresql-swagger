package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface NoteRepository extends CrudRepository<NoteEntity, Long> {

//    @Query("SELECT ue.notes FROM UserEntity ue LEFT JOIN FETCH ue.notes n WHERE ue.id = :userId")
    @Query("SELECT ue.notes FROM UserEntity ue LEFT JOIN ue.notes n WHERE ue.id = :userId") // we can create a DB VIEW for this
    Collection<NoteEntity> findByUserId(Long userId);
}
