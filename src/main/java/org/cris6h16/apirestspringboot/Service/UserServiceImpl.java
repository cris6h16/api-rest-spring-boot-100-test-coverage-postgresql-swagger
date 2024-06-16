package org.cris6h16.apirestspringboot.Service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchEmailUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchPasswordUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchUsernameUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicRoleDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.EmailAlreadyExistException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.UserNotFoundException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.UsernameAlreadyExistsException;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/**
 * An implementation of {@link UserService} interface
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    public static final String anyDTOMsg = Cons.User.DTO.ANY_RELATED_DTO_WITH_USER_NULL;
    public static final String invalidIdMsg = Cons.CommonInEntity.ID_INVALID;
    public static final String genericMsg = Cons.Response.ForClient.GENERIC_ERROR;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public Long create(@Valid @NotNull(message = anyDTOMsg) CreateUserDTO dto) {
        RoleEntity role = roleRepository.findByName(ERole.ROLE_USER)
                .orElse(RoleEntity.builder().name(ERole.ROLE_USER).build());

        UserEntity saved = userRepository.saveAndFlush(UserEntity.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .roles(new HashSet<>(Collections.singleton(role)))
                .createdAt(new Date())
                .build());

        return saved.getId();
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public PublicUserDTO get(@NotNull(message = invalidIdMsg) Long id) {
        Optional<UserEntity> userO = userRepository.findById(id);
        if (userO.isEmpty()) throw new UserNotFoundException();

        return createPublicUserDTO(userO.get());
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void delete(@NotNull(message = invalidIdMsg) Long id) {
        if (!userRepository.existsById(id)) throw new UserNotFoundException();
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public List<PublicUserDTO> get(@NotNull(message = genericMsg) Pageable pageable) {
        Pageable pag = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "id"))
        );

        return userRepository.findAll(pag).stream()
                .map(this::createPublicUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void patchUsernameById(@NotNull(message = invalidIdMsg) Long id,
                                  @Valid @NotNull(message = anyDTOMsg) PatchUsernameUserDTO dto) {
        if (!userRepository.existsById(id)) throw new UserNotFoundException();
        if (userRepository.existsByUsername(dto.getUsername())) throw new UsernameAlreadyExistsException();
        userRepository.updateUsernameById(dto.getUsername(), id);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void patchEmailById(@NotNull(message = invalidIdMsg) Long id,
                               @Valid @NotNull(message = anyDTOMsg) PatchEmailUserDTO dto) { // todo: see if @Valid can replace the @not null
        if (!userRepository.existsById(id)) throw new UserNotFoundException();
        if (userRepository.existsByEmail(dto.getEmail())) throw new EmailAlreadyExistException();
        userRepository.updateEmailById(dto.getEmail(), id);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void patchPasswordById(@NotNull(message = invalidIdMsg) Long id,
                                  @Valid @NotNull(message = anyDTOMsg) PatchPasswordUserDTO dto) {
        if (!userRepository.existsById(id)) throw new UserNotFoundException();
        userRepository.updatePasswordById(passwordEncoder.encode(dto.getPassword()), id);
    }

    /**
     * Create a {@link PublicUserDTO} from a {@link UserEntity}<br>
     * - If {@code user == null} return {@code dto} empty.<br>
     * - If {@code user.roles == null } return {@code dto} with roles empty.<br>
     *
     * @param user to create the {@link PublicUserDTO}
     * @return {@link PublicUserDTO}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private PublicUserDTO createPublicUserDTO(UserEntity user) {
        if (user == null) return PublicUserDTO.builder().build();

        boolean rolesNull = (user.getRoles() == null); // roles --> is EAGER
        Set<PublicRoleDTO> roles = rolesNull ?
                (new HashSet<>(0)) :
                (user.getRoles().stream()
                        .map(role -> new PublicRoleDTO(role.getName()))
                        .collect(Collectors.toSet()));

        return PublicUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roles)
                .build();
    }
}