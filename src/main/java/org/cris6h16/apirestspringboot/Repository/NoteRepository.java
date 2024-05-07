package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.Optional;

public interface NoteRepository extends CrudRepository<NoteEntity, Long>, PagingAndSortingRepository<NoteEntity, Long>{

    //    @Query("SELECT ue.notes FROM UserEntity ue LEFT JOIN FETCH ue.notes n WHERE ue.id = :userId")

    @Query("SELECT n FROM NoteEntity n WHERE n.user.id = ?1")
    Collection<NoteEntity> findByUserId(Long userID);

    Optional<NoteEntity> findByIdAndUserId(Long id, Long userId);

    Page<NoteEntity> findByUserId(Long userID, Pageable pageable);

}
