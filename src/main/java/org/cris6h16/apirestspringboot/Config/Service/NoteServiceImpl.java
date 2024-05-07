package org.cris6h16.apirestspringboot.Config.Service;

import jakarta.persistence.SecondaryTable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Config.Service.Interfaces.NoteService;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements NoteService {
    NoteRepository noteRepository;
    UserRepository userRepository;

    public NoteServiceImpl(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }


    @Override
    @PreAuthorize("@AuthCustomResponses.checkIfIsAuthenticated()")
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public ResponseEntity<Void> createNote(@Valid @NotNull CreateNoteDTO note) {
        Long id = ((UserWithId) (SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getId();

        UserEntity user = userRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")); // if was deleted while it was authenticated, avoid it with some like that: `.maximumSessions(1).maxSessionsPreventsLogin(true)`

        NoteEntity noteEntity = NoteEntity.builder()
                .title(note.getTitle())
                .content(note.getContent())
                .user(user)
                .build();

        noteRepository.save(noteEntity);

        return ResponseEntity.created(URI.create("/api/notes/" + noteEntity.getId()))
                .build();
    }

    @Override
    @PreAuthorize("@AuthCustomResponses.checkIfIsAuthenticated()")
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public ResponseEntity<List<PublicNoteDTO>> getPage(Pageable pageable) {

        Long id = ((UserWithId) (SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getId();
        Page<NoteEntity> page = noteRepository.findByUserId(id,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "id"))
                ));

        List<PublicNoteDTO> pnDTOs = page.stream()
                .map(note -> PublicNoteDTO.builder()
                        .id(note.getId())
                        .title(note.getTitle())
                        .content(note.getContent())
                        .createdAt(note.getCreatedAt())
                        .updatedAt(note.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(pnDTOs);
    }

    @Override
    @PreAuthorize("@AuthCustomResponses.checkIfIsAuthenticated()")
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public ResponseEntity<PublicNoteDTO> getNoteById(Long noteId) {
        Long userId = ((UserWithId) (SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getId();

        NoteEntity note = noteRepository
                .findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        return ResponseEntity.ok(PublicNoteDTO.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build());
    }
}
