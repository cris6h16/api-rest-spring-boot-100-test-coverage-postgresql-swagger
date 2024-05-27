package org.cris6h16.apirestspringboot.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.AbstractServiceExceptionWithStatus;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.Common.InvalidIdException;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.NoteServiceTraversalException;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserService.UserNotFoundException;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Interfaces.NoteService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cris6h16.apirestspringboot.Constants.Cons.Note.Fails.NOT_FOUND;

@Service
@Slf4j
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteServiceImpl(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public Long create(CreateNoteDTO note, Long userId) {
        try {
            validateId(userId);
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(UserNotFoundException::new);

            NoteEntity noteEntity = NoteEntity.builder()
                    .title(note.getTitle())
                    .content(note.getContent())
                    .updatedAt(new Date())
                    .build();

            noteEntity.setUser(user);
            noteRepository.save(noteEntity);

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
    public List<PublicNoteDTO> getPage(Pageable pageable, Long userId) {

        Page<NoteEntity> page = noteRepository.findByUserId(userId,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "id"))
                ));

        return page.stream() //TODO: see the generated query of pageable (because they say that is not necessary include in the @Query)
                .map(note -> PublicNoteDTO.builder()
                        .id(note.getId())
                        .title(note.getTitle())
                        .content(note.getContent())
                        .updatedAt(note.getUpdatedAt())
                        .updatedAt(note.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public PublicNoteDTO get(Long noteId, Long userId) {

        NoteEntity note = noteRepository
                .findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND));

        return PublicNoteDTO.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .updatedAt(note.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void put(Long noteId, CreateNoteDTO note, Long userId) {

        UserEntity user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, Cons.User.Fails.NOT_FOUND));

        NoteEntity noteEntity = noteRepository
                .findByIdAndUserId(noteId, userId) // If exists get it, else create ==> exists || create ==> same ID
                .orElse(NoteEntity.builder()
                        .id(noteId)
                        .title(note.getTitle())
                        .content(note.getContent())
                        .updatedAt(new Date())
                        .build()
                );
        noteEntity.setTitle(note.getTitle());
        noteEntity.setContent(note.getContent());

        noteRepository.save(noteEntity);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void delete(Long noteId, Long userId) {

        NoteEntity note = noteRepository
                .findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND));

        noteRepository.delete(note);
    }

    NoteServiceTraversalException getTraversalException(Exception e) {
        String forClient = "";
        HttpStatus recommendedStatus = null;

        // data integrity violations { not blank, invalid email, max length, etc }
        if (e instanceof ConstraintViolationException && forClient.isBlank()) {
            recommendedStatus = HttpStatus.BAD_REQUEST;
            Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) e).getConstraintViolations();

            if (!violations.isEmpty()) forClient = violations.iterator().next().getMessage();
            else log.error("ConstraintViolationException: {}", e.getMessage());
        }

        // validations here { user not found }
        if (e instanceof AbstractServiceExceptionWithStatus && forClient.isBlank()) {
            recommendedStatus = ((AbstractServiceExceptionWithStatus) e).getRecommendedStatus();
            forClient = e.getMessage();
        }

        // unhandled exceptions -> generic error
        if (forClient.isBlank()) {
            if (recommendedStatus == null) recommendedStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            forClient = Cons.Response.ForClient.GENERIC_ERROR;
            log.error("Unhandled exception: {}", e.getMessage());
        }

        return new NoteServiceTraversalException(forClient, recommendedStatus);
    }

    void validateId(Long id) {
        if (id == null || id <= 0) throw new InvalidIdException();
    }
}
