package org.cris6h16.apirestspringboot.DTOs.Patch;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static org.cris6h16.apirestspringboot.Constants.Cons.User.Validations.EMAIL_INVALID_MSG;
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Validations.EMAIL_IS_BLANK_MSG;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PatchEmailUserDTO {
    @Email(message = EMAIL_INVALID_MSG)// --> null is valid
    @NotBlank(message = EMAIL_IS_BLANK_MSG)
    private String email;
}
