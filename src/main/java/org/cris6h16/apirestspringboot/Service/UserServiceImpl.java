package org.cris6h16.apirestspringboot.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Exceptions.service.UserServiceException;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;
import org.cris6h16.apirestspringboot.Service.PreExceptions.NotFoundException;
import org.cris6h16.apirestspringboot.DTOs.*;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
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
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Constrains.EMAIL_UNIQUE_MSG;
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
            verifyDTO(dto, "create");

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
            throw exceptionHandled(e);
        }
    }

    UserServiceException exceptionHandled(Exception e) {
        String forClient;
        HttpStatus recommendedStatus;

        if (e instanceof DataIntegrityViolationException) {
            recommendedStatus = HttpStatus.CONFLICT;
            boolean inUsername = thisContains(e.getMessage(), "unique constraint", USERNAME_UNIQUE_NAME);
            boolean inEmail = thisContains(e.getMessage(), "unique constraint", EMAIL_UNIQUE_NAME);
            boolean isHandledUniqueViolation = inUsername || inEmail;
            if (isHandledUniqueViolation) {
                forClient = inUsername ? USERNAME_UNIQUE_MSG : EMAIL_UNIQUE_MSG;
            } else {
                forClient = Cons.ExceptionHandler.defMsg.DataIntegrityViolation.UNHANDLED;
                log.error(forClient, e.getMessage());
            }
        }

        else if (e instanceof IllegalArgumentException){
            forClient = e.getMessage();
            recommendedStatus = HttpStatus.BAD_REQUEST;
        }

        return new UserServiceException()
    }

    public boolean thisContains(String msg, String... strings) {
        boolean contains = true;
        for (String s : strings) {
            contains = contains && msg.contains(s);
        }
        return contains;
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_UNCOMMITTED, //reading data without modifications
            rollbackFor = Exception.class
    )
    public PublicUserDTO get(Long id) {
        UserEntity usr = userRepository.findById(id).orElseThrow(NotFoundException.User::new);

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
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void update(Long id, CreateUpdateUserDTO dto) {
        UserEntity usr = userRepository.findById(id).orElseThrow(NotFoundException.User::new);

        boolean updateUsername = dto.getUsername() != null && !dto.getUsername().isBlank() && !dto.getUsername().equals(usr.getUsername());
        boolean updateEmail = dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equals(usr.getEmail());
        boolean updatePassword = dto.getPassword() != null && !dto.getPassword().isBlank() && !passwordEncoder.matches(dto.getPassword(), usr.getPassword());
        boolean wantUpdate = updateUsername || updateEmail || updatePassword;

        if (!wantUpdate) return;

        if (updateUsername) usr.setUsername(dto.getUsername());
        if (updateEmail) usr.setEmail(dto.getEmail());
        if (updatePassword) {
            verifyDTO(dto);// throws if not
            usr.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        usr.setUpdatedAt(new Date());
        userRepository.save(usr);
    }

    /**
     * The unique earlier verification, this is in the service layer due that the password pass to repository encrypted
     * then it means that the password always will have a length greater than 8
     *
     * @param dto the user to verify its password
     * @throws Exception                if the operation passed is not implemented
     * @throws IllegalArgumentException and its message If is business logic error
     */
    void verifyDTO(CreateUpdateUserDTO dto, String operation) throws Exception {
        if (dto == null) throw new IllegalArgumentException(Cons.User.DTO.NULL);

        boolean isCreate = operation.equals("create");
        boolean passInvalid = dto.getPassword() == null || dto.getPassword().length() < 8;

        if (isCreate && passInvalid) throw new IllegalArgumentException(PASS_IS_TOO_SHORT_MSG);
        else throw new Exception("Operation not implemented");
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void delete(Long id) {
        UserEntity usr = userRepository.findById(id).orElseThrow(NotFoundException.User::new);
        userRepository.delete(usr);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public List<PublicUserDTO> get(Pageable pageable) {

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

    }

}
