package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    Page<NoteEntity> findByUser(UserEntity user, Pageable pageable);

    Optional<NoteEntity> findByIdAndUserId(Long noteId, Long userId);

    boolean existsByIdAndUserId(Long noteId, Long userId);

    void deleteByIdAndUserId(Long noteId, Long userId);
    // todo: deleteById this and see how use the examples<s>

    <S extends NoteEntity> Page<S> findByUserId(Long userId, Pageable pageable);


    default void deleteAll(){
        deleteAllInBatch();
    }
}

