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

@Service
@Slf4j
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final ServiceUtils serviceUtils;
    private final UserServiceImpl userService;

    public NoteServiceImpl(NoteRepository noteRepository,
                           UserRepository userRepository, ServiceUtils serviceUtils, UserServiceImpl userService) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.serviceUtils = serviceUtils;
        this.userService = userService;
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

    //todo: doc trhows
    private UserEntity validateIdAndGetUser(Long userId) {
        return this.userService.validateIdAndGetUser(userId);
    }


    private NoteServiceTransversalException getTraversalException(Exception e) {
        return (NoteServiceTransversalException) this.serviceUtils.createATraversalExceptionHandled(e, false);
    }

    private void validateDTONotNull(CreateNoteDTO dto) {
        if (dto == null) throw new CreateNoteDTOIsNullException();
    }

    // todo: doc this
    public NoteEntity validateIdAndGetNote(Long noteId, UserEntity userInDB) {
        this.serviceUtils.validateId(noteId);
        return noteRepository
                .findByIdAndUser(noteId, userInDB)
                .orElseThrow(NoteNotFoundException::new);
    }

    private void validateId(Long noteId) {
        if (noteId == null || noteId <= 0) throw new InvalidIdException();
    }
}
