package org.cris6h16.apirestspringboot.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.RoleDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.*;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserService.CreateUpdateDTOIsNullException;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserService.InvalidIdException;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserService.PasswordTooShortException;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserService.UserNotFoundException;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cris6h16.apirestspringboot.Constants.Cons.User.Constrains.*;
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    ObjectMapper objectMapper;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public Long create(CreateUpdateUserDTO dto) {
        try {
            verifyPasswordInDTO(dto);

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
            verifyId(id);
            UserEntity usr = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

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
            verifyId(id);

            UserEntity usr = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

            boolean updateUsername = dto.getUsername() != null && !dto.getUsername().isBlank() && !dto.getUsername().equals(usr.getUsername());
            boolean updateEmail = dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equals(usr.getEmail());
            boolean updatePassword = dto.getPassword() != null && !dto.getPassword().isBlank() && !passwordEncoder.matches(dto.getPassword(), usr.getPassword());
            boolean wantUpdate = updateUsername || updateEmail || updatePassword;

            if (!wantUpdate) return;

            if (updateUsername) usr.setUsername(dto.getUsername());
            if (updateEmail) usr.setEmail(dto.getEmail());
            if (updatePassword) {
                verifyPasswordInDTO(dto);// throws if not
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
            verifyId(id);
            UserEntity usr = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
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
    void verifyPasswordInDTO(CreateUpdateUserDTO dto) {
        if (dto == null) throw new CreateUpdateDTOIsNullException();
        boolean passInvalid = (dto.getPassword() == null || dto.getPassword().length() < 8);
        if (passInvalid) throw new PasswordTooShortException();
    }

    /**
     * @param id the id to verify
     * @throws AbstractServiceExceptionWithStatus if the id is invalid
     */
    void verifyId(Long id) {
        if (id == null || id > 0) throw new InvalidIdException();
    }



    UserServiceTraversalException createATraversalExceptionHandled(@NotNull Exception e) {
        String forClient = ""; // PD: verification based on: .isBlank(), dont add generic message here
        HttpStatus recommendedStatus = null; // also here, but with null

        // unique violations { primary key, unique constraints }
        if (e instanceof DuplicateKeyException) {
            recommendedStatus = HttpStatus.CONFLICT;
            boolean inUsername = thisContains(e.getMessage(), USERNAME_UNIQUE_NAME);
            boolean inEmail = thisContains(e.getMessage(), EMAIL_UNIQUE_NAME);
            boolean isHandledUniqueViolation = inUsername || inEmail;

            if (isHandledUniqueViolation) forClient = inUsername ? USERNAME_UNIQUE_MSG : EMAIL_UNIQUE_MSG;
            else log.error("DuplicateKeyException: {}", e.getMessage());
        }

        // data integrity violations { not blank, invalid email, max length, etc }
        if (e instanceof ConstraintViolationException && forClient.isBlank()) {
            recommendedStatus = HttpStatus.BAD_REQUEST;
            Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) e).getConstraintViolations();

            if (!violations.isEmpty()) forClient = violations.iterator().next().getMessage();
            else log.error("ConstraintViolationException: {}", e.getMessage());
        }

        // business logic errors pre-handled { password too short }
        if (e instanceof IllegalArgumentException && forClient.isBlank()) {
            recommendedStatus = HttpStatus.BAD_REQUEST;
            if (thisContains(e.getMessage(), PASS_IS_TOO_SHORT_MSG)) forClient = PASS_IS_TOO_SHORT_MSG;
            else log.error("IllegalArgumentException: {}", e.getMessage());
        }

        // customs exceptions with status { user not found, password too short }
        if (e instanceof AbstractServiceExceptionWithStatus) {
            recommendedStatus = ((AbstractServiceExceptionWithStatus) e).getRecommendedStatus();
            forClient = e.getMessage();
        }

        // unhandled exceptions -> generic error
        if (forClient.isBlank()) {
            if (recommendedStatus == null) recommendedStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            forClient = Cons.Response.ForClient.GENERIC_ERROR;
            log.error("Unhandled exception: {}", e.getMessage());
        }


        return new UserServiceTraversalException(forClient, recommendedStatus);
    }


    public boolean thisContains(String msg, String... strings) {
        boolean contains = true;
        for (String s : strings) {
            contains = contains && msg.contains(s);
        }
        return contains;
    }


}
