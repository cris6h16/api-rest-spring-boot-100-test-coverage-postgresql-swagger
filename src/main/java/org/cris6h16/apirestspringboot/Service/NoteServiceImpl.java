package org.cris6h16.apirestspringboot.Service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService.NoteNotFoundException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.UserNotFoundException;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Interfaces.NoteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An implementation of {@link NoteService} interface // todo: explain why I dont use userservice -> excess of transactions
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Service
@Slf4j
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    public static final String invalidIdMsg = Cons.CommonInEntity.ID_INVALID;
    public static final String nullDTOMsg = Cons.Note.DTO.NULL;
    public static final String genericMsg = Cons.Response.ForClient.GENERIC_ERROR;

    public NoteServiceImpl(NoteRepository noteRepository,
                           UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public Long create(@Valid @NotNull(message = nullDTOMsg) CreateNoteDTO note,
                       @NotNull(message = invalidIdMsg) Long userId) {
        Optional<UserEntity> user = userRepository.findById(userId);
        if (user.isEmpty()) throw new UserNotFoundException();

        NoteEntity noteEntity = NoteEntity.builder()
                .title(note.getTitle())
                .content(note.getContent() == null ? " " : note.getContent())
                .updatedAt(new Date())
                .user(user.get())
                .build();
        noteEntity = noteRepository.saveAndFlush(noteEntity); // Changed from `.save()` to one with flush, due to testing with H2

        return noteEntity.getId();
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public PublicNoteDTO get(@NotNull(message = invalidIdMsg) Long noteId,
                             @NotNull(message = invalidIdMsg) Long userId) {
        if (!userRepository.existsById(userId)) throw new UserNotFoundException();
        Optional<NoteEntity> n = noteRepository.findByIdAndUserId(noteId, userId);
        if (n.isEmpty()) throw new NoteNotFoundException();

        return createPublicNoteDTO(n.get());
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void put(@NotNull(message = invalidIdMsg) Long noteId,
                    @NotNull(message = invalidIdMsg) Long userId,
                    @Valid @NotNull(message = nullDTOMsg) CreateNoteDTO dto) {
        Optional<UserEntity> user = userRepository.findById(userId);
        if (user.isEmpty()) throw new UserNotFoundException();

        NoteEntity n = noteRepository
                .findByIdAndUserId(noteId, userId) // If exists get it, else create ==> exists || create ==> same ID
                .orElse(NoteEntity.builder().id(noteId).build());

        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setUpdatedAt(new Date());

        noteRepository.saveAndFlush(n);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void delete(@NotNull(message = invalidIdMsg) Long noteId,
                       @NotNull(message = invalidIdMsg) Long userId) {
        if (!userRepository.existsById(userId)) throw new UserNotFoundException();
        if (!noteRepository.existsByIdAndUserId(noteId, userId)) throw new NoteNotFoundException();
        noteRepository.deleteByIdAndUserId(noteId, userId);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public List<PublicNoteDTO> getPage(@NotNull(message = genericMsg) Pageable pageable,
                                       @NotNull(message = invalidIdMsg) Long userId) {
        if (!userRepository.existsById(userId)) throw new UserNotFoundException();

        Page<NoteEntity> page = noteRepository.findByUserId(userId,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "id"))
                ));

        return page.stream()
                .map(this::createPublicNoteDTO)
                .collect(Collectors.toList());
    }


    private PublicNoteDTO createPublicNoteDTO(NoteEntity n) {
        if (n == null) return PublicNoteDTO.builder().build();
        if (n.getId() == null) n.setId(-1L);
        if (n.getTitle() == null) n.setTitle("");
        if (n.getContent() == null) n.setContent("");

        return PublicNoteDTO.builder()
                .title(n.getTitle())
                .content(n.getContent())
                .updatedAt(n.getUpdatedAt())
                .id(n.getId())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
