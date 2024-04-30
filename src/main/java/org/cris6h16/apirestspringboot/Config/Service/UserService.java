package org.cris6h16.apirestspringboot.Config.Service;

import org.cris6h16.apirestspringboot.Config.Service.Exceptions.EmailAlreadyExistsException;
import org.cris6h16.apirestspringboot.Config.Service.Exceptions.EmailUsernamePasswordAreRequiredException;
import org.cris6h16.apirestspringboot.Config.Service.Exceptions.PasswordTooShortException;
import org.cris6h16.apirestspringboot.Config.Service.Exceptions.UsernameAlreadyExistsException;
import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> createUser(CreateUserDTO dto) {
        Optional<RoleEntity> role = roleRepository.findByName(ERole.USER);

        UserEntity user = new UserEntity(
                null,
                dto.getUsername(),
                dto.getEmail(),
                dto.getPassword(),
                null,
                null,
                null,

                );
    }
}
