package org.cris6h16.apirestspringboot.Services;

import org.cris6h16.apirestspringboot.DTOs.Creation.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.Interfaces.Notes.NullAttributesBlanker;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.Common.InvalidIdException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService.AnyNoteDTOIsNullException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService.NoteNotFoundException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.NoteService.TitleMaxLengthFailException;
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

import static org.cris6h16.apirestspringboot.Constants.Cons.Note.Validations.MAX_TITLE_LENGTH;

/**
 * An implementation of {@link NoteService} interface
 *
 * @since 1.0
 */
@Service
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteServiceImpl(NoteRepository noteRepository,
                           UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Long create(CreateNoteDTO dto, Long userId) {
        prepareAndVerifyDTOAndIds(dto, userId);

        UserEntity user = getUserById(userId);

        NoteEntity noteEntity = NoteEntity.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .updatedAt(new Date())
                .user(user)
                .build();
        noteEntity = noteRepository.saveAndFlush(noteEntity);

        return noteEntity.getId();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public PublicNoteDTO getByIdAndUserId(Long noteId, Long userId) {
        verifyId(userId, noteId);

        NoteEntity noteEntity = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(NoteNotFoundException::new);

        return createPublicNoteDTO(noteEntity);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void putByIdAndUserId(Long noteId, Long userId, CreateNoteDTO dto) {
        prepareAndVerifyDTOAndIds(dto, userId, noteId);

        UserEntity user = getUserById(userId);

        NoteEntity noteEntity = noteRepository.findByIdAndUserId(noteId, userId)
                .orElse(NoteEntity.builder().id(noteId).user(user).build());

        noteEntity.setTitle(dto.getTitle());
        noteEntity.setContent(dto.getContent());
        noteEntity.setUpdatedAt(new Date());

        noteRepository.saveAndFlush(noteEntity);
    }


    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteByIdAndUserId(Long noteId, Long userId) {
        verifyId(userId, noteId);
        if (!userRepository.existsById(userId)) throw new UserNotFoundException();
        if (!noteRepository.existsByIdAndUserId(noteId, userId)) throw new NoteNotFoundException();

        noteRepository.deleteByIdAndUserId(noteId, userId);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public List<PublicNoteDTO> getPage(Pageable pageable, Long userId) {
        if (pageable == null) throw new IllegalArgumentException("Pageable can't be null");
        verifyId(userId);
        if (!userRepository.existsById(userId)) throw new UserNotFoundException();

        Page<NoteEntity> page = noteRepository.findByUserId(userId,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort()));

        return page.stream()
                .map(this::createPublicNoteDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public void deleteAll() {
        noteRepository.deleteAll();
    }

    private UserEntity getUserById(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private PublicNoteDTO createPublicNoteDTO(NoteEntity noteEntity) {
        return PublicNoteDTO.builder()
                .title(Optional.ofNullable(noteEntity.getTitle()).orElse(""))
                .content(Optional.ofNullable(noteEntity.getContent()).orElse(""))
                .id(Optional.ofNullable(noteEntity.getId()).orElse(-1L))
                .updatedAt(noteEntity.getUpdatedAt())
                .build();
    }

    private void prepareAndVerifyDTOAndIds(CreateNoteDTO dto, Long... ids) {
        verifyId(ids);
        _dtoNotNull(dto);
        _prepareAttributes(dto);

        _verifyTitle(dto.getTitle());
        _verifyContent(dto.getContent());
    }

    private void verifyId(Long... ids) {
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new InvalidIdException();
            }
        }
    }

    private void _verifyContent(String content) {
        // at the moment we don't have any validation for content
    }

    private void _verifyTitle(String title) {
        if (title.trim().length() > MAX_TITLE_LENGTH) {
            throw new TitleMaxLengthFailException();
        }
    }

    private <T> void _dtoNotNull(T dto) {
        if (dto == null) throw new AnyNoteDTOIsNullException();
    }

    private <T extends NullAttributesBlanker> void _prepareAttributes(T dto) {
        dto.toBlankNullAttributes();
    }
}
