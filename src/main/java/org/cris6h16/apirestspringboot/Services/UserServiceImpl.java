package org.cris6h16.apirestspringboot.Services;

import jakarta.validation.*;
import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchEmailUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchPasswordUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchUsernameUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicRoleDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.*;
import org.cris6h16.apirestspringboot.Repositories.RoleRepository;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.cris6h16.apirestspringboot.Services.Interfaces.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.cris6h16.apirestspringboot.Constants.Cons.User.Validations.*;


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
    private final Validator validator;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, Validator validator) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public Long create(CreateUserDTO dto, ERole... roles) { // @Valid doesn't work here
        if (roles.length == 0) throw new IllegalArgumentException("Roles can't be empty");

        UserEntity user;
        Set<RoleEntity> rolesEntities = new HashSet<>(roles.length);

        attributesNotBlankNotNull(dto);
        trimAndToLower(dto);
        validatePassword(dto.getPassword());

        for (ERole role : roles) {
            RoleEntity roleDB = roleRepository
                    .findByName(role)
                    .orElse(RoleEntity.builder().name(role).build());
            rolesEntities.add(roleDB);
        }

        user = UserEntity.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .roles(rolesEntities)
                .createdAt(new Date())
                .build();
        userRepository.saveAndFlush(user);

        return user.getId();
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public PublicUserDTO getById(Long id) {
        Optional<UserEntity> userO = userRepository.findById(id);
        if (userO.isEmpty()) throw new UserNotFoundException();

        return createPublicUserDTO(userO.get());
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) throw new UserNotFoundException();
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public List<PublicUserDTO> getPage(Pageable pageable) {
        if (pageable == null) throw new IllegalArgumentException("Pageable can't be null");

        // all elements are verified in the creation of the PageRequest then it is not necessary to verify them again
        Pageable pag = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
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
    public void patchUsernameById(Long id, PatchUsernameUserDTO dto) { // @Valid doesn't work here
        attributesNotBlankNotNull(dto);

        dto.setUsername(dto.getUsername().toLowerCase().trim());
        if (!userRepository.existsById(id)) throw new UserNotFoundException();
        if (userRepository.existsByUsername(dto.getUsername())) throw new UsernameAlreadyExistsException();
        userRepository.updateUsernameById(dto.getUsername(), id);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void patchEmailById(Long id, PatchEmailUserDTO dto) {
        attributesNotBlankNotNull(dto);

        dto.setEmail(dto.getEmail().toLowerCase().trim());
        if (!userRepository.existsById(id)) throw new UserNotFoundException();
        if (userRepository.existsByEmail(dto.getEmail())) throw new EmailAlreadyExistException();
        userRepository.updateEmailById(dto.getEmail(), id);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void patchPasswordById(Long id, PatchPasswordUserDTO dto) {
        attributesNotBlankNotNull(dto);

        dto.setPassword(dto.getPassword().trim());
        if (dto.getPassword().length() < MIN_PASSWORD_LENGTH) throw new PasswordTooShortException();
        if (!userRepository.existsById(id)) throw new UserNotFoundException();
        userRepository.updatePasswordById(passwordEncoder.encode(dto.getPassword()), id);
    }

    @Override
    @Transactional(
            isolation = Isolation.SERIALIZABLE,
            rollbackFor = Exception.class
    )
    public void deleteAll() {
        userRepository.deleteAll(); // n + 1
    }


    private void trimAndToLower(CreateUserDTO dto) {
        dto.setEmail(dto.getEmail().toLowerCase().trim());
        dto.setPassword(dto.getPassword().toLowerCase().trim());
        dto.setUsername(dto.getUsername().toLowerCase().trim());
    }

    private void validatePassword(String password) {
        boolean passFailLength = password != null && password.length() >= MIN_PASSWORD_LENGTH;
        if (passFailLength) throw new PasswordTooShortException();
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
                .notes(new HashSet<>(0))
                .build();
    }


    private void attributesNotBlankNotNull(CreateUserDTO dto) {
        if (dto == null) throw new AnyUserDTOIsNullException();

        boolean emailBlank = dto.getEmail() == null || dto.getEmail().isBlank();
        boolean passwordBlank = dto.getPassword() == null || dto.getPassword().isBlank();
        boolean usernameBlank = dto.getUsername() == null || dto.getUsername().isBlank();

        if (emailBlank) throw new UserEmailIsBlankException();
        if (passwordBlank) throw new PasswordTooShortException();
        if (usernameBlank) throw new UsernameIsBlankException();
    }
}