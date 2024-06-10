package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.Iterator;
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

    /**
     * <b>JUST FOR TESTING</b> you should never try to retrieve all notes
     *
     * @param user user
     * @return All notes owned by the user
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    List<NoteEntity> findByUser(UserEntity user);

    /**
     * Find a {@link NoteEntity} by its {@code id} and {@code user}
     *
     * @param noteId id of the note to retrieve
     * @param user   user owner of the note
     * @return {@link Optional} of {@link NoteEntity} if found, {@link Optional#empty()} otherwise
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    Optional<NoteEntity> findByIdAndUser(Long noteId, UserEntity user);

    /**
     * Find all {@link NoteEntity} owned by the provided {@link UserEntity}
     * in a paginated way
     *
     * @param user     {@link UserEntity} owner of the notes
     * @param pageable {@link PageRequest}
     * @return {@link Page} of {@link NoteEntity} owned by the user
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    Page<NoteEntity> findByUser(UserEntity user, Pageable pageable);
}

