package org.cris6h16.apirestspringboot.Service.Interfaces;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.UpdateUserDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    ResponseEntity<Void> createUser(@NotNull @Valid CreateUserDTO dto);

    ResponseEntity<PublicUserDTO> getByIdLazy(Long id);

    ResponseEntity<Void> updateUser(Long id, @NotNull @Valid UpdateUserDTO dto);

    ResponseEntity<Void> deleteUser(Long id);

    ResponseEntity<List<PublicUserDTO>> getUsers(Pageable pageable);

    // for test purposes
}
