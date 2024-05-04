package org.cris6h16.apirestspringboot.Config.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.cris6h16.apirestspringboot.Config.Service.PreExceptions.PasswordIsTooShortException;
import org.cris6h16.apirestspringboot.Config.Service.PreExceptions.AlreadyExistsException;
import org.cris6h16.apirestspringboot.DTOs.*;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.cris6h16.apirestspringboot.Config.Service.CustomAuthHandler.MyAuthorizationService;

import java.net.URI;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    ObjectMapper objectMapper;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> createUser(@NotNull @Valid CreateUserDTO dto) {
        Optional<RoleEntity> roles = roleRepository.findByName(ERole.USER);
        if (roles.isEmpty()) roles = Optional.of(new RoleEntity(null, ERole.USER));
        if (dto.getPassword() == null || dto.getPassword().length() < 8)
            throw new PasswordIsTooShortException(); //TODO: docs why we handle it here directly (encryption)

        UserEntity user = new UserEntity(
                null,
                dto.getUsername(),
                passwordEncoder.encode(dto.getPassword()), // default is bcrypt
                dto.getEmail(),
                null,
                null,
                null,
                Set.of(roles.get()),
                null
        );
        userRepository.save(user);

        return ResponseEntity.created(URI.create("/users/" + user.getId())).build();
    }


    @PreAuthorize("#username == authentication.principal.username") // Try not to use this, always try to reference using Primary Key.. This also works remember that is UNIQUE
    // TODO: doc about what i learnt -> throw custom exceptions when @PreAuthorize("#username == authentication.principal.username") fails( create a method apart & call with try-catch)
    public ResponseEntity<PublicUserDTO> getByUsername(String username) {
        Optional<UserEntity> user = userRepository.findByUsername(username);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        // get notes
        Set<PublicNoteDTO> notes = user.get().getNotes().stream()
                .map(note -> new PublicNoteDTO(
                        note.getTitle(),
                        note.getContent(),
                        note.getCreatedAt(),
                        note.getUpdatedAt(),
                        note.getDeletedAt()
                ))
                .collect(Collectors.toSet());
        // get roles
        Set<RoleDTO> roles = user.get().getRoles().stream()
                .map(role -> new RoleDTO(role.getName()))
                .collect(Collectors.toSet());

        PublicUserDTO userDTO = new PublicUserDTO(
                user.get().getId(),
                user.get().getUsername(),
                user.get().getEmail(),
                user.get().getCreatedAt(),
                user.get().getDeletedAt(),
                roles,
                notes
        );
        return ResponseEntity.ok(userDTO);
    }


    //    @PreAuthorize("#id == authentication.principal.id") // TODO: doc about the custom impl with id or any
    @PreAuthorize("@authResponses.checkIfIsAuthenticated() && @authResponses.checkIfIsOwnerOfThisId(#id)") // return exception with a message in response body

    public ResponseEntity<Void> updateUser(Long id, UpdateUserDTO dto) {
        Optional<UserEntity> user = userRepository.findById(id);
        if (user.isEmpty()) return ResponseEntity.notFound().build();

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
