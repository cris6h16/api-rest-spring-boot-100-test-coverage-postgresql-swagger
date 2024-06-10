package org.cris6h16.apirestspringboot.Service;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.Common.InvalidIdException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService.CreateNoteDTOIsNullException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService.NoteNotFoundException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteServiceTransversalException;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Interfaces.NoteService;
import org.cris6h16.apirestspringboot.Service.Utils.ServiceUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation of {@link NoteService} interface
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Service
@Slf4j
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final ServiceUtils serviceUtils;
    private final UserServiceImpl userServiceImpl;

    public NoteServiceImpl(NoteRepository noteRepository,
                           UserRepository userRepository, ServiceUtils serviceUtils, UserServiceImpl userService) {
        this.noteRepository = noteRepository;
        this.serviceUtils = serviceUtils;
        this.userServiceImpl = userService;
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public Long create(CreateNoteDTO note, Long userId) {
        try {
            UserEntity user = validateIdAndGetUser(userId);
            validateDTONotNull(note);

            NoteEntity noteEntity = NoteEntity.builder()
                    .title(note.getTitle()) // If blank || null db will throw
                    .content(note.getContent() == null ? "" : note.getContent())
                    .updatedAt(new Date())
                    .build();

            noteEntity.setUser(user);
            noteEntity = noteRepository.saveAndFlush(noteEntity); // Changed from `.save()` to one with flush, due to testing with H2

            return noteEntity.getId();

        } catch (Exception e) {
            throw getTraversalException(e);
        }
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public PublicNoteDTO get(Long noteId, Long userId) {
        try {
            UserEntity user = validateIdAndGetUser(userId);
            NoteEntity note = validateIdAndGetNote(noteId, user);

            return PublicNoteDTO.builder()
                    .id(note.getId())
                    .title(note.getTitle())
                    .content(note.getContent())
                    .updatedAt(note.getUpdatedAt())
                    .build();

        } catch (Exception e) {
            throw getTraversalException(e);
        }
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void put(Long noteId, CreateNoteDTO dto, Long userId) {
        try {
            UserEntity usr = validateIdAndGetUser(userId);
            validateId(noteId);
            NoteEntity noteEntity = noteRepository
                    .findByIdAndUser(noteId, usr) // If exists get it, else create ==> exists || create ==> same ID
                    .orElse(NoteEntity.builder()
                            .id(noteId)
                            .title(dto.getTitle())
                            .content(dto.getContent())
                            .updatedAt(new Date())
                            .build()
                    );

            noteEntity.setTitle(dto.getTitle());
            noteEntity.setContent(dto.getContent());

            noteRepository.saveAndFlush(noteEntity);

        } catch (Exception e) {
            throw getTraversalException(e);
        }
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void delete(Long noteId, Long userId) {
        try {
            UserEntity usr = validateIdAndGetUser(userId);
            NoteEntity note = validateIdAndGetNote(noteId, usr);

            noteRepository.delete(note);

        } catch (Exception e) {
            throw getTraversalException(e);
        }
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public List<PublicNoteDTO> getPage(Pageable pageable, Long userId) {
        try {
            UserEntity usr = validateIdAndGetUser(userId);

            Page<NoteEntity> page = noteRepository.findByUser(usr,
                    PageRequest.of(
                            pageable.getPageNumber(),
                            pageable.getPageSize(),
                            pageable.getSortOr(Sort.by(Sort.Direction.ASC, "id"))
                    ));

            return page.stream()
                    .map(note -> PublicNoteDTO.builder()
                            .id(note.getId())
                            .title(note.getTitle())
                            .content(note.getContent())
                            .updatedAt(note.getUpdatedAt())
                            .updatedAt(note.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw getTraversalException(e);
        }
    }

    /**
     * Wrapper for {@link UserServiceImpl#validateIdAndGetUser(Long)} to validate the user ID and retrieve the user.
     *
     * @param userId the ID of the user to validate and retrieve
     * @return the {@link UserEntity} corresponding to the validated ID
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private UserEntity validateIdAndGetUser(Long userId) {
        return this.userServiceImpl.validateIdAndGetUser(userId);
    }


    /**
     * Wrapper for {@link ServiceUtils#createATraversalExceptionHandled(Exception, boolean)}
     *
     * @param e the exception, used to create above-mentioned exception
     * @return the created {@link NoteServiceTransversalException} exception
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private NoteServiceTransversalException getTraversalException(Exception e) {
        return (NoteServiceTransversalException) this.serviceUtils.createATraversalExceptionHandled(e, false);
    }


    /**
     * Validates that the DTO is not null
     *
     * @param dto to see if it is null
     * @throws CreateNoteDTOIsNullException if the DTO is null
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private void validateDTONotNull(CreateNoteDTO dto) {
        if (dto == null) throw new CreateNoteDTOIsNullException();
    }

    /**
     * Validates the note ID and retrieves the note<br>
     * 1. Validates the passed {@code noteId} with {@link ServiceUtils#validateId(Long)}<br>
     * 2. Retrieves the note from db where {@code (note.id = noteId) && (note.user = userInDB)}
     * if it wasn't found, it throws.
     *
     * @param noteId   the ID of the note to validate and retrieve
     * @param userInDB the user to which the note belongs
     * @return the {@link NoteEntity} corresponding to the validated ID
     * @throws NoteNotFoundException if the note was not found
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    public NoteEntity validateIdAndGetNote(Long noteId, UserEntity userInDB) {
        this.serviceUtils.validateId(noteId);
        return noteRepository
                .findByIdAndUser(noteId, userInDB)
                .orElseThrow(NoteNotFoundException::new);
    }

    /**
     * Validates the note ID
     *
     * @param noteId the ID of the note to validate
     * @throws InvalidIdException if the passed {@code id} considered invalid
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private void validateId(Long noteId) {
        if (noteId == null || noteId <= 0) throw new InvalidIdException();
    }
}
