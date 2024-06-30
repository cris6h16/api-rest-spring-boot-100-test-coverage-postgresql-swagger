package org.cris6h16.apirestspringboot.Repositories;

import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

/**
 * Repository for {@link NoteEntity}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public interface NoteRepository extends
        JpaRepository<NoteEntity, Long>,
        PagingAndSortingRepository<NoteEntity, Long> {

    Optional<NoteEntity> findByIdAndUserId(Long noteId, Long userId);

    boolean existsByIdAndUserId(Long noteId, Long userId);

    void deleteByIdAndUserId(Long noteId, Long userId);

    <S extends NoteEntity> Page<S> findByUserId(Long userId, Pageable pageable);

}

