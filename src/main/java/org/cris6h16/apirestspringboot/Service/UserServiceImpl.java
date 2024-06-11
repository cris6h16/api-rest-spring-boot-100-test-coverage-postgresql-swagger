package org.cris6h16.apirestspringboot.Service;

import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicRoleDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.Common.InvalidIdException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.CreateUpdateDTOIsNullException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.PasswordTooShortException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.UserNotFoundException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserServiceTransversalException;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;
import org.cris6h16.apirestspringboot.Service.Utils.ServiceUtils;
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
    ServiceUtils serviceUtils;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           ServiceUtils serviceUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.serviceUtils = serviceUtils;
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public Long create(CreateUpdateUserDTO dto) {
        try {
            verifyDTONotNullAndPassword(dto);

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

        } catch (Exception e) {
            throw createATraversalExceptionHandled(e);
        }
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public PublicUserDTO get(Long id) {
        try {
            UserEntity usr = validateIdAndGetUser(id);
            return createPublicUserDTO(usr);

        } catch (Exception e) {
            throw createATraversalExceptionHandled(e);
        }
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void update(Long id, CreateUpdateUserDTO dto) {
        try {
            UserEntity fromDB = validateIdAndGetUser(id);
            if (dto == null) throw new CreateUpdateDTOIsNullException();

            // get the values from dto
            String username = dto.getUsername();
            String email = dto.getEmail();
            String pass = dto.getPassword();

            // check which values are trying to update
            boolean updateUsername = (username != null) &&
                    (!username.isBlank()) &&
                    (!username.equalsIgnoreCase(fromDB.getUsername()));

            boolean updateEmail = (email != null) &&
                    (!email.isBlank()) &&
                    (!email.equalsIgnoreCase(fromDB.getEmail()));

            boolean updatePassword = (pass != null) &&
                    (!pass.isBlank()) &&
                    (!passwordEncoder.matches(pass, fromDB.getPassword()));

            boolean wantUpdate = updateUsername || updateEmail || updatePassword;

            // if no values to update the do nothing
            if (!wantUpdate) return;

            // update the corresponding values
            if (updateUsername) fromDB.setUsername(username);
            if (updateEmail) fromDB.setEmail(email);
            if (updatePassword) {
                verifyDTONotNullAndPassword(dto);
                fromDB.setPassword(passwordEncoder.encode(pass));
            }
            // set the date of update
            fromDB.setUpdatedAt(new Date());

            // save the changes
            userRepository.saveAndFlush(fromDB);

        } catch (Exception e) {
            throw createATraversalExceptionHandled(e);
        }
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void delete(Long id) {
        try {
            UserEntity usr = validateIdAndGetUser(id);
            userRepository.delete(usr);

        } catch (Exception e) {
            throw createATraversalExceptionHandled(e);
        }
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public List<PublicUserDTO> get(Pageable pageable) {
        try {
            Pageable pag = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    pageable.getSortOr(Sort.by(Sort.Direction.ASC, "id"))
            );

            return userRepository.findAll(pag).stream()
                    .map(this::createPublicUserDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw createATraversalExceptionHandled(e);
        }
    }


    /**
     * The unique earlier verification, this is in the service layer
     * due that the password pass to repository encrypted then
     * it means that the password always will have a length
     * greater than 8
     *
     * @param dto the user to verify its password
     * @throws CreateUpdateDTOIsNullException If {@code dto} is null
     * @throws PasswordTooShortException      if {@code dto.password} is null or too short
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private void verifyDTONotNullAndPassword(CreateUpdateUserDTO dto) {
        if (dto == null) throw new CreateUpdateDTOIsNullException();
        boolean passInvalid = (dto.getPassword() == null || dto.getPassword().length() < 8);
        if (passInvalid) throw new PasswordTooShortException();
    }


    /**
     * Validate id and get user from repository
     *
     * @param userId to validate
     * @return {@link UserEntity}
     * @throws UserNotFoundException if user wasn't found
     * @throws InvalidIdException    if {@code userId} is invalid
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    public UserEntity validateIdAndGetUser(Long userId) {
        serviceUtils.validateId(userId);
        return userRepository
                .findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * delegate the creation of a {@link UserServiceTransversalException}
     * to {@link ServiceUtils#createATraversalExceptionHandled(Exception, boolean)}
     *
     * @param e the exception to handle
     * @return {@link UserServiceTransversalException} with a status code and message
     * ready for pass to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private UserServiceTransversalException createATraversalExceptionHandled(Exception e) {
        return (UserServiceTransversalException) serviceUtils.createATraversalExceptionHandled(e, true);
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
