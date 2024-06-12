package org.cris6h16.apirestspringboot.Service.Interfaces;

import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteServiceTransversalException;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

//todo: remove unnecessary imports in all classes

/**
 * Service layer for {@link NoteRepository}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public interface NoteService {
    /**
     * Get a note by:<br>
     * {@code (note.id == noteId) && (note.user.id == userId)}
     *
     * @param noteId of the note to get
     * @param userId of the user that owns the note
     * @return the note that has the provided {@code noteId} and {@code userId}, as a {@link PublicNoteDTO}
     * @throws NoteServiceTransversalException with the proper
     *                                         status code and message ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    PublicNoteDTO get(Long noteId, Long userId);

    /**
     * Create a new note with the provided {@code note} setting the {@code userId} as the owner
     *
     * @param note   to create
     * @param userId of the user that will own the note
     * @return the {@code id} of the created note
     * @throws NoteServiceTransversalException with the proper
     *                                         status code and message ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    Long create(CreateNoteDTO note, Long userId);

    /**
     * PUT the passed note where:<br>
     * {@code (note.id == noteId) && (note.user.id == userId)}
     *
     * @param noteId of the note to update
     * @param note   containing the new data to update
     * @param userId of the user that owns the note
     * @throws NoteServiceTransversalException with the proper
     *                                         status code and message ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void put(Long noteId, CreateNoteDTO note, Long userId);

    /**
     * DELETE the note where:<br>
     * {@code (note.id == noteId) && (note.user.id == userId)}
     *
     * @param noteId of the note to delete
     * @param userId of the user that owns the note
     * @throws NoteServiceTransversalException with the proper
     *                                         status code and message ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void delete(Long noteId, Long userId);

    /**
     * Get all the notes owned by the user with the provided {@code userId}
     *
     * @param pageable page request
     * @param userId   of the user that owns the notes
     * @return a list of {@link PublicNoteDTO} with the notes of the user
     * @throws NoteServiceTransversalException with the proper
     *                                         status code and message ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    List<PublicNoteDTO> getPage(Pageable pageable, Long userId);

}
