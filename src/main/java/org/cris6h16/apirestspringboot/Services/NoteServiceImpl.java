package org.cris6h16.apirestspringboot.Services;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.Common.InvalidIdException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService.AnyNoteDTOIsNullException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService.NoteNotFoundException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService.TitleIsBlankException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.UserNotFoundException;
import org.cris6h16.apirestspringboot.Repositories.NoteRepository;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.cris6h16.apirestspringboot.Services.Interfaces.NoteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Long create(CreateNoteDTO note, Long userId) { // @Valid doesn't work here

        verifyId(userId);
        attributesNotBlankNotNull(note); // content == null then content = ""

        Optional<UserEntity> user = userRepository.findById(userId);
        if (user.isEmpty()) throw new UserNotFoundException();

        NoteEntity noteEntity = NoteEntity.builder()
                .title(note.getTitle())
                .content(note.getContent())
                .updatedAt(new Date())
                .user(user.get())
                .build();
        noteEntity = noteRepository.saveAndFlush(noteEntity); // Changed from `.save()` to one with flush, due to testing with H2

        return noteEntity.getId();
    }

    private void verifyId(Long... ids) {
        for (Long id : ids) {
            if (id == null || id <= 0) throw new InvalidIdException();
        }
    }

    private void attributesNotBlankNotNull(CreateNoteDTO dto) {
        if (dto == null) throw new AnyNoteDTOIsNullException();
        if (dto.getTitle() == null || dto.getTitle().isBlank()) throw new TitleIsBlankException();
        if (dto.getContent() == null) dto.setContent("");
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public PublicNoteDTO getByIdAndUserId(Long noteId, Long userId) {
        verifyId(userId, noteId);

        Optional<NoteEntity> n = noteRepository.findByIdAndUserId(noteId, userId);
        if (n.isEmpty()) throw new NoteNotFoundException();

        return createPublicNoteDTO(n.get());
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void putByIdAndUserId(Long noteId, Long userId, CreateNoteDTO dto) {
        verifyId(userId, noteId);
        attributesNotBlankNotNull(dto);

        Optional<UserEntity> user = userRepository.findById(userId);
        if (user.isEmpty()) throw new UserNotFoundException();

        NoteEntity n = noteRepository
                .findByIdAndUserId(noteId, userId) // If exists getById it, else create ==> exists || create ==> same ID
                .orElse(NoteEntity.builder().id(noteId).build());

        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setUpdatedAt(new Date());
        n.setUser(user.get());

        noteRepository.saveAndFlush(n);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void deleteByIdAndUserId(Long noteId, Long userId) {
        verifyId(userId, noteId);
        if (!userRepository.existsById(userId)) throw new UserNotFoundException();
        if (!noteRepository.existsByIdAndUserId(noteId, userId)) throw new NoteNotFoundException();

        noteRepository.deleteByIdAndUserId(noteId, userId);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public List<PublicNoteDTO> getPage(Pageable pageable, Long userId) {
        if (pageable == null) throw new IllegalArgumentException("Pageable can't be null");
        verifyId(userId);
        if (!userRepository.existsById(userId)) throw new UserNotFoundException();

        Page<NoteEntity> page = noteRepository.findByUserId(userId,
                // all elements are verified in the creation of the PageRequest then it is not necessary to verify them again ( IllegalArgumentException )
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSort()
                )
        );

        return page.stream()
                .map(this::createPublicNoteDTO)
                .collect(Collectors.toList());
    }

    @Transactional(
            isolation = Isolation.SERIALIZABLE,
            rollbackFor = Exception.class
    )
    @Override
    public void deleteAll() {
        noteRepository.deleteAll();
    }


    private PublicNoteDTO createPublicNoteDTO(NoteEntity n) {
        if (n == null) n = NoteEntity.builder().build();
        if (n.getId() == null) n.setId(-1L);
        if (n.getTitle() == null) n.setTitle("");
        if (n.getContent() == null) n.setContent("");

        return PublicNoteDTO.builder()
                .title(n.getTitle())
                .content(n.getContent())
                .id(n.getId())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
