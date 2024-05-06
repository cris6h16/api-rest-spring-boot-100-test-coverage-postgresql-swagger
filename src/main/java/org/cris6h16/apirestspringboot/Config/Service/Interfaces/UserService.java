package org.cris6h16.apirestspringboot.Config.Service.Interfaces;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.UpdateUserDTO;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<Void> createUser(@NotNull @Valid CreateUserDTO dto);
    ResponseEntity<PublicUserDTO> getByIdLazy(Long id);
    ResponseEntity<Void> updateUser(Long id, @NotNull @Valid UpdateUserDTO dto);

}
