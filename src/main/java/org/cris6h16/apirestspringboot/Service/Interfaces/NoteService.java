package org.cris6h16.apirestspringboot.Service.Interfaces;

import org.cris6h16.apirestspringboot.DTOs.Creation.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


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
     * @param noteId of the note to getById
     * @param userId of the user that owns the note
     * @return the note that has the provided {@code noteId} and {@code userId}, as a {@link PublicNoteDTO}
     * @throws NoteServiceTransversalException with the proper
     *                                         status code and message ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    PublicNoteDTO getByIdAndUserId(Long noteId, Long userId);

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
    void put(Long noteId, Long userId, CreateNoteDTO note);

    /**
     * DELETE the note where:<br>
     * {@code (note.id == noteId) && (note.user.id == userId)}
     *
     * @param noteId of the note to deleteById
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


    void deleteAll();

}
