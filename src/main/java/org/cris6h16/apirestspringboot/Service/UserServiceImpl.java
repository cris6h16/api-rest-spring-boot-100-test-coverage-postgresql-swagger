package org.cris6h16.apirestspringboot.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.RoleDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.AbstractServiceExceptionWithStatus;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserService.CreateUpdateDTOIsNullException;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserService.PasswordTooShortException;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserService.UserNotFoundException;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserServiceTraversalException;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;
import org.cris6h16.apirestspringboot.Service.Utils.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

            RoleEntity roles = roleRepository.findByName(ERole.ROLE_USER)
                    .orElse(RoleEntity.builder().name(ERole.ROLE_USER).build());

            UserEntity saved = userRepository.save(UserEntity.builder()
                    .username(dto.getUsername())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .email(dto.getEmail())
                    .roles(Set.of(roles)) //cascading
                    .createdAt(new Date())
                    .build());

            return saved.getId();

        } catch (Exception e) {
            throw createATraversalExceptionHandled(e);
        }
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_UNCOMMITTED, // read uncommitted to avoid locks
            rollbackFor = Exception.class
    )
    public PublicUserDTO get(Long id) {
        try {
            UserEntity usr = validateIdAndGetUser(id);

            // get roles --> is EAGER
            Set<RoleDTO> roles = usr.getRoles().stream()
                    .map(role -> new RoleDTO(role.getName()))
                    .collect(Collectors.toSet());

            return PublicUserDTO.builder()
                    .id(usr.getId())
                    .username(usr.getUsername())
                    .email(usr.getEmail())
                    .createdAt(usr.getCreatedAt())
                    .updatedAt(usr.getUpdatedAt())
                    .roles(roles)
                    .build();

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
            UserEntity usr = validateIdAndGetUser(id);

            boolean updateUsername = dto.getUsername() != null && !dto.getUsername().isBlank() && !dto.getUsername().equals(usr.getUsername());
            boolean updateEmail = dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equals(usr.getEmail());
            boolean updatePassword = dto.getPassword() != null && !dto.getPassword().isBlank() && !passwordEncoder.matches(dto.getPassword(), usr.getPassword());
            boolean wantUpdate = updateUsername || updateEmail || updatePassword;

            if (!wantUpdate) return;

            if (updateUsername) usr.setUsername(dto.getUsername());
            if (updateEmail) usr.setEmail(dto.getEmail());
            if (updatePassword) {
                verifyDTONotNullAndPassword(dto);
                usr.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            usr.setUpdatedAt(new Date());
            userRepository.save(usr);

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
                    .map(user -> PublicUserDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .createdAt(user.getCreatedAt())
                            .updatedAt(user.getUpdatedAt())
                            .roles(user.getRoles().stream()
                                    .map(role -> new RoleDTO(role.getName()))
                                    .collect(Collectors.toSet()))
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw createATraversalExceptionHandled(e);
        }
    }


    /**
     * The unique earlier verification, this is in the service layer due that the password pass to repository encrypted
     * then it means that the password always will have a length greater than 8
     *
     * @param dto the user to verify its password
     * @throws AbstractServiceExceptionWithStatus If dto is null || password in dto is invalid
     */
    void verifyDTONotNullAndPassword(CreateUpdateUserDTO dto) {
        if (dto == null) throw new CreateUpdateDTOIsNullException();
        boolean passInvalid = (dto.getPassword() == null || dto.getPassword().length() < 8);
        if (passInvalid) throw new PasswordTooShortException();
    }


    /**
     * Validate id  and get user from repository
     *
     * @param userId to validate
     * @return {@link UserEntity}
     * @throws AbstractServiceExceptionWithStatus if user not found or id is invalid
     */
    public UserEntity validateIdAndGetUser(Long userId) {
        serviceUtils.validateId(userId);
        return userRepository
                .findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }


    private UserServiceTraversalException createATraversalExceptionHandled(Exception e) {
        return (UserServiceTraversalException) serviceUtils.createATraversalExceptionHandled(e, true);
    }
}
