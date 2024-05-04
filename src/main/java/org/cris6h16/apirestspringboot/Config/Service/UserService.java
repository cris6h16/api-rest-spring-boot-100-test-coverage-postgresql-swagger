package org.cris6h16.apirestspringboot.Config.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.cris6h16.apirestspringboot.Controllers.PreExceptions.PasswordIsTooShortException;
import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.RoleDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public ResponseEntity<Void> createUser(@NotNull @Valid CreateUserDTO dto) {
        Optional<RoleEntity> roles = roleRepository.findByName(ERole.USER);
        if (roles.isEmpty()) roles = Optional.of(new RoleEntity(null, ERole.USER));
        if (dto.getPassword() == null || dto.getPassword().length() < 8) throw new PasswordIsTooShortException(); //TODO: docs why we handle it here directly (encryption)

        UserEntity user = new UserEntity(
                null,
                dto.getUsername(),
                passwordEncoder.encode(dto.getPassword()), // default is bcrypt
                dto.getEmail(),
                new Date(System.currentTimeMillis()),
                null,
                null,
                Set.of(roles.get()),
                null
        );
        userRepository.save(user);

        return ResponseEntity.created(URI.create("/users/" + user.getId())).build();
    }


    //getByUsername
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
}
