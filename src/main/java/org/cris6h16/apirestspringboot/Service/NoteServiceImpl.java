package org.cris6h16.apirestspringboot.Service;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.NoteService.NoteNotFoundException;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.NoteServiceTraversalException;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Interfaces.NoteService;
import org.cris6h16.apirestspringboot.Service.Utils.ServiceUtils;
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
import java.util.stream.Collectors;

import static org.cris6h16.apirestspringboot.Constants.Cons.Note.Fails.NOT_FOUND;

@Service
@Slf4j
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final ServiceUtils serviceUtils;

    public NoteServiceImpl(NoteRepository noteRepository,
                           UserRepository userRepository,
                           ServiceUtils serviceUtils) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.serviceUtils = serviceUtils;
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public Long create(CreateNoteDTO note, Long userId) {
        try {
            UserEntity user = validateIdAndGetUser(userId);

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

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public PublicNoteDTO get(Long noteId, Long userId) {
        try {
            UserEntity user = validateIdAndGetUser(noteId);

            NoteEntity note = noteRepository
                    .findByIdAndUser(noteId, user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND));

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
    public void put(Long noteId, CreateNoteDTO note, Long userId) {
        try {
            UserEntity usr = validateIdAndGetUser(userId);

            NoteEntity noteEntity = noteRepository
                    .findByIdAndUser(noteId, usr) // If exists get it, else create ==> exists || create ==> same ID
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

            NoteEntity note = noteRepository.findByIdAndUser(noteId, usr)
                    .orElseThrow(NoteNotFoundException::new);

            noteRepository.delete(note);

        } catch (Exception e) {
            throw getTraversalException(e);
        }
    }

    //todo: doc trhows
    private UserEntity validateIdAndGetUser(Long userId) {
        return this.serviceUtils.validateIdAndGetUser(userId);
    }


    private NoteServiceTraversalException getTraversalException(Exception e) {
        return (NoteServiceTraversalException) this.serviceUtils.createATraversalExceptionHandled(e, false);
    }
}
