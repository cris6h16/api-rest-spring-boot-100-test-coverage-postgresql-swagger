package org.cris6h16.apirestspringboot.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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