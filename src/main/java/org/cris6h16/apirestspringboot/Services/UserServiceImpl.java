package org.cris6h16.apirestspringboot.Services;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Interfaces.Users.NotNullAttributesToLowerConverter;
import org.cris6h16.apirestspringboot.DTOs.Interfaces.Users.NotNullAttributesTrimmer;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchEmailUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchPasswordUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchUsernameUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicRoleDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.Common.InvalidIdException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.*;
import org.cris6h16.apirestspringboot.Repositories.RoleRepository;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.cris6h16.apirestspringboot.Services.Interfaces.UserService;
import org.springframework.data.domain.Page;
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
    public Long create(CreateUserDTO dto, ERole... roles) { // @Valid doesn't work here
        if (roles == null || roles.length == 0) {
            throw new IllegalArgumentException("Roles can't be empty"); // implementation fail, we don't show the message to the user
        }
        dtoNotNull(dto);
        prepareAttributes(dto);

        validateUsername(dto.getUsername());
        validateEmail(dto.getEmail());
        validatePassword(dto.getPassword());

        UserEntity user;
        Set<RoleEntity> rolesEntities = new HashSet<>(roles.length);

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
        user = userRepository.saveAndFlush(user); // reassigned for testing purposes

        return user.getId();
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public PublicUserDTO getById(Long id) {
        verifyId(id); // coming from controller is never reached ( if is logged in then the principal.id is valid, and if try pass an invalid id then the security in the controller endpoint will deny the access (principal.id == idRequested ? grantAccess : denyAccess) )

        Optional<UserEntity> userO = userRepository.findById(id); // coming from controller is never reached ( controllers has the verification as principal.id == idRequested ? grantAccess : denyAccess )
        if (userO.isEmpty())
            throw new UserNotFoundException(); // if our app is not stateless && is multi-session, we may have that exception

        return createPublicUserDTO(userO.get());
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void deleteById(Long id) {
        verifyId(id); // never reached coming from controller
        if (!userRepository.existsById(id)) throw new UserNotFoundException(); // never reached coming from controller
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public Page<PublicUserDTO> getPage(Pageable pageable) {
        if (pageable == null) throw new IllegalArgumentException("Pageable can't be null");

        // all elements are verified in the creation of the PageRequest then it is not necessary to verify them again ( IllegalArgumentException )
        Pageable pag = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );

        return userRepository.findAll(pag)
                .map(this::createPublicUserDTO);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void patchUsernameById(Long id, PatchUsernameUserDTO dto) { // @Valid doesn't work here
        verifyId(id); // never reached coming from controller
        dtoNotNull(dto); // never reached coming from controller (required = true)
        prepareAttributes(dto);

        validateUsername(dto.getUsername());

        if (!userRepository.existsById(id))
            throw new UserNotFoundException(); // never reached if is stateless and single-session
        if (userRepository.existsByUsername(dto.getUsername())) throw new UsernameAlreadyExistsException();
        userRepository.updateUsernameById(dto.getUsername(), id);
    }

    private void verifyId(Long id) {
        if (id == null || id <= 0) throw new InvalidIdException();
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void patchEmailById(Long id, PatchEmailUserDTO dto) {
        verifyId(id); // never reached coming from controller
        dtoNotNull(dto); // never reached coming from controller
        prepareAttributes(dto);

        validateEmail(dto.getEmail());

        if (!userRepository.existsById(id))
            throw new UserNotFoundException(); // never reached if is stateless and single-session
        if (userRepository.existsByEmail(dto.getEmail())) throw new EmailAlreadyExistException();
        userRepository.updateEmailById(dto.getEmail(), id);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void patchPasswordById(Long id, PatchPasswordUserDTO dto) {
        verifyId(id); // never reached coming from controller
        dtoNotNull(dto); // never reached coming from controller
        prepareAttributes(dto);

        validatePassword(dto.getPassword());

        if (!userRepository.existsById(id))
            throw new UserNotFoundException(); // never reached if is stateless and single-session
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


    private void validatePassword(String password) {
        if (password == null) throw new PlainPasswordLengthException();

        boolean isTooShort = password.trim().length() < MIN_PASSWORD_LENGTH;
        boolean isTooLong = password.trim().length() > MAX_PASSWORD_LENGTH_PLAIN;

        boolean lengthFail = isTooShort || isTooLong;
        if (lengthFail) throw new PlainPasswordLengthException();
    }

    private void validateUsername(String username) {
        if (username == null) throw new UsernameLengthException();

        boolean isTooShort = username.trim().length() < MIN_USERNAME_LENGTH;
        boolean isTooLong = username.trim().length() > MAX_USERNAME_LENGTH;

        boolean lengthFail = isTooShort || isTooLong;
        if (lengthFail) throw new UsernameLengthException();
    }

    private void validateEmail(String email) {
        if (email == null) throw new EmailIsInvalidException();

        boolean isTooShort = email.trim().length() < MIN_EMAIL_LENGTH;
        boolean isTooLong = email.trim().length() > MAX_EMAIL_LENGTH;
        boolean isEmail = email.trim().matches("^\\S+@\\S+\\.\\S+$"); //--> ^ = start of the string, \S = any non-whitespace character, + = one or more, @ = @, \S = any non-whitespace character, + = one or more, \. = ., \S = any non-whitespace character, + = one or more, $ = end of the string

        boolean emailInvalid = isTooShort || isTooLong || !isEmail;
        if (emailInvalid) throw new EmailIsInvalidException();
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

    private <T> void dtoNotNull(T dto) {
        if (dto == null) throw new AnyUserDTOIsNullException();
    }

    private <T> void prepareAttributes(T dto) {
//       instanceof NullAttributesBlanker // not implemented in the DTOs used here ( due that these attributes has e.g. Dates, and I can't set a Date to "" )
        if (dto instanceof NotNullAttributesTrimmer obj) obj.trimNotNullAttributes();
        if (dto instanceof NotNullAttributesToLowerConverter obj) obj.toLowerCaseNotNullAttributes();
    }

}