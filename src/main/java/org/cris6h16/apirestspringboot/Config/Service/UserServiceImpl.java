package org.cris6h16.apirestspringboot.Config.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.cris6h16.apirestspringboot.Config.Service.Interfaces.UserService;
import org.cris6h16.apirestspringboot.Config.Service.PreExceptions.PasswordIsTooShortException;
import org.cris6h16.apirestspringboot.Config.Service.PreExceptions.AlreadyExistsException;
import org.cris6h16.apirestspringboot.DTOs.*;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    ObjectMapper objectMapper;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    @PreAuthorize("permitAll()")
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public ResponseEntity<Void> createUser(@NotNull @Valid CreateUserDTO dto) {
        Optional<RoleEntity> roles = roleRepository.findByName(ERole.ROLE_USER);
        if (roles.isEmpty()) roles = Optional.of(new RoleEntity(null, ERole.ROLE_USER));
        if (dto.getPassword() == null || dto.getPassword().length() < 8)
            throw new PasswordIsTooShortException(); //TODO: docs why we handle it here directly (encryption)

        UserEntity user = UserEntity.builder()
                .id(null)
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .createdAt(null)
                .updatedAt(null)
                .deletedAt(null)
                .roles(Set.of(roles.get()))
                .notes(null)
                .build();

        userRepository.save(user);

        return ResponseEntity.created(URI.create("/users/" + user.getId())).build();
    }


    @Override
    @PreAuthorize("@AuthCustomResponses.checkIfIsAuthenticated() && @AuthCustomResponses.checkIfIsOwnerOfThisId(#id)")
    @Transactional(
            isolation = Isolation.READ_UNCOMMITTED, //reading data without modifications
            rollbackFor = Exception.class
    )
    public ResponseEntity<PublicUserDTO> getByIdLazy(Long id) {
        Optional<UserEntity> user = userRepository.findById(id);
        if (user.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"); // if was deleted from DB while it was authenticated, avoid it with some like that: `.maximumSessions(1).maxSessionsPreventsLogin(true)`

        // notes --> is LAZY
        Set<PublicNoteDTO> notes = new HashSet<>(0);

        // get roles --> is EAGER
        Set<RoleDTO> roles = user.get().getRoles().stream()
                .map(role -> new RoleDTO(role.getName()))
                .collect(Collectors.toSet());

        PublicUserDTO userDTO = PublicUserDTO.builder()
                .id(user.get().getId())
                .username(user.get().getUsername())
                .email(user.get().getEmail())
                .createdAt(user.get().getCreatedAt())
                .updatedAt(user.get().getUpdatedAt())
                .roles(roles)
                .notes(notes)
                .build();

        return ResponseEntity.ok(userDTO);
    }


    @Override
    //    @PreAuthorize("#id == authentication.principal.id") // TODO: doc about the custom impl with id or any
    @PreAuthorize("@AuthCustomResponses.checkIfIsAuthenticated() && @AuthCustomResponses.checkIfIsOwnerOfThisId(#id)")
    // return exception with a message in response body
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public ResponseEntity<Void> updateUser(Long id, @NotNull @Valid UpdateUserDTO dto) {
        Optional<UserEntity> user = userRepository.findById(id);
        if (user.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"); // if was deleted from DB while it was authenticated, avoid it with some like that: `.maximumSessions(1).maxSessionsPreventsLogin(true)`

        boolean updateUsername = dto.getUsername() != null && !dto.getUsername().isBlank();
        boolean updateEmail = dto.getEmail() != null && !dto.getEmail().isBlank();
        boolean updatePassword = dto.getPassword() != null && !dto.getPassword().isBlank();


        if (updateUsername) {
            if (userRepository.findByUsername(dto.getUsername()).isPresent())
                throw new AlreadyExistsException("Username");
            user.get().setUsername(dto.getUsername());
        }

        if (updateEmail) {
            if (userRepository.findByEmail(dto.getEmail()).isPresent())
                throw new AlreadyExistsException("Email");
            user.get().setEmail(dto.getEmail());
        }
        if (updatePassword) {
            if (dto.getPassword().length() < 8) throw new PasswordIsTooShortException();
            user.get().setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        userRepository.save(user.get());

        return ResponseEntity.noContent().build();
    }
}