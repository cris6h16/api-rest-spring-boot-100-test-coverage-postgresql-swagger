package org.cris6h16.apirestspringboot.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;
import org.cris6h16.apirestspringboot.Service.PreExceptions.PasswordIsTooShortException;
import org.cris6h16.apirestspringboot.Service.PreExceptions.AlreadyExistsException;
import org.cris6h16.apirestspringboot.DTOs.*;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
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
        Optional<RoleEntity> roles = roleRepository.findByName(ERole.ROLE_USER);
        if (roles.isEmpty()) roles = Optional.of(new RoleEntity(null, ERole.ROLE_USER));
        if (dto.getPassword() == null || dto.getPassword().length() < 8) throw new PasswordIsTooShortException();

        UserEntity user = UserEntity.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .roles(Set.of(roles.get()))
                .build();

        userRepository.save(user);

        return user.getId();
    }


    @Override
    @Transactional(
            isolation = Isolation.READ_UNCOMMITTED, //reading data without modifications
            rollbackFor = Exception.class
    )
    public PublicUserDTO get(Long id) {
        UserEntity usr = userRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, Cons.User.Fails.NOT_FOUND));

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

        UserEntity usr = userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, Cons.User.Fails.NOT_FOUND));

        boolean updateUsername = dto.getUsername() != null && !dto.getUsername().isBlank();
        boolean updateEmail = dto.getEmail() != null && !dto.getEmail().isBlank();
        boolean updatePassword = dto.getPassword() != null && !dto.getPassword().isBlank();


        if (updateUsername) {
            if (userRepository.findByUsername(dto.getUsername()).isPresent())
                throw new AlreadyExistsException.Username();
            usr.setUsername(dto.getUsername());
        }

        if (updateEmail) {
            if (userRepository.findByEmail(dto.getEmail()).isPresent())
                throw new AlreadyExistsException.Email();
            usr.setEmail(dto.getEmail());
        }
        if (updatePassword) {
            if (dto.getPassword().length() < 8) throw new PasswordIsTooShortException();
            usr.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        userRepository.save(usr);
    }

    @Override
    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class
    )
    public void delete(Long id) {
        UserEntity usr = userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, Cons.User.Fails.NOT_FOUND));
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
