package org.cris6h16.apirestspringboot.DTOs;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreateUserDTO {
    private String username;
    private String password;
    private String email;
}
