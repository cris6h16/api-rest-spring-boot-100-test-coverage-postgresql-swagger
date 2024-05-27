package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.springframework.context.annotation.Profile;
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

public interface NoteRepository extends JpaRepository<NoteEntity, Long>,
        PagingAndSortingRepository<NoteEntity, Long> {

    /**
     * <b>JUST FOR TESTING</b> you should never try to retrieve all notes
     * @param user user
     * @return All notes owned by the user
     */
    List<NoteEntity> findByUser(UserEntity user);

    Optional<NoteEntity> findByIdAndUser(Long noteId, UserEntity user);

    Page<NoteEntity> findByUser(UserEntity user, Pageable pageable);





    // PLZ avoid UNIDIRECTIONALs to avoid this kind of queries
//    @Query("SELECT n FROM UserEntity u JOIN u.notes n WHERE u.id = ?1")
//    List<NoteEntity> findByUserId(Long userID);
//
//    @Query("SELECT n FROM UserEntity u JOIN u.notes n WHERE n.id = ?1 AND  u.id = ?2")
//    Optional<NoteEntity> findByIdAndUserId(Long noteId, Long userId);
//
//    @Query("SELECT n FROM UserEntity u JOIN u.notes n WHERE u.id = ?1")
//    Page<NoteEntity> findByUserId(Long userID, Pageable pageable);

}
