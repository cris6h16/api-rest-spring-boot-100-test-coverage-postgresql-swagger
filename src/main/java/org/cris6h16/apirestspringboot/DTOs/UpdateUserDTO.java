package org.cris6h16.apirestspringboot.DTOs;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class UpdateUserDTO { //TODO: find if is possible simplify the constraints annotations


    public UpdateUserDTO(Long id) {
        this.id = id;
    }

    private Long id;
    private String username;
    private String password;
    private String email;
    private Date updatedAt;


}
