package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<NoteEntity, Long>, PagingAndSortingRepository<NoteEntity, Long> {

    List<NoteEntity> findByUserId(Long userID);

    Optional<NoteEntity> findByIdAndUserId(Long id, Long userId);

    Page<NoteEntity> findByUserId(Long userID, Pageable pageable);

}
