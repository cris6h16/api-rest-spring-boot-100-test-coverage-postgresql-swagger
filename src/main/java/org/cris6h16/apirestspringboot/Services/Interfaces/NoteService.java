package org.cris6h16.apirestspringboot.Services.Interfaces;

import org.cris6h16.apirestspringboot.DTOs.Creation.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Repositories.NoteRepository;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;


/**
 * Service layer for {@link NoteRepository}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public interface NoteService {
    /**
     * Get a note by id and user id, return if {@code retrievedNote.user.id == userId}
     *
     * @param noteId note id
     * @param userId user id that owns the note
     * @return a {@link PublicNoteDTO} with the note data
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    PublicNoteDTO getByIdAndUserId(Long noteId, Long userId);

    /**
     * Create a new note
     *
     * @param note   the data of the new note
     * @param userId the id of the user that is creating the note
     * @return the id of the new note created
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    Long create(CreateNoteDTO note, Long userId);

    /**
     * PUT a note.<br>
     * If exists, update the note where {@code (note.id == noteId) && (note.user.id == userId)}
     *
     * @param noteId note id
     * @param userId user id that owns the note
     * @param note   the note data to be PUT
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void putByIdAndUserId(Long noteId, Long userId, CreateNoteDTO note);

    /**
     * DELETE a note.<br>
     * where {@code (note.id == noteId) && (note.user.id == userId)}
     *
     * @param noteId note id of the note to delete
     * @param userId user id that owns the note
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void deleteByIdAndUserId(Long noteId, Long userId);

    /**
     * Get a page of notes owned by a user
     *
     * @param pageable the page request
     * @param userId   the id of the user that owns the notes
     * @return a list of {@link PublicNoteDTO} with the notes data
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    List<PublicNoteDTO> getPage(Pageable pageable, Long userId);

    /**
     * Delete all notes
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void deleteAll();

}
