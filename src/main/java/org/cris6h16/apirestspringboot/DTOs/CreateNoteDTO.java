package org.cris6h16.apirestspringboot.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CreateNoteDTO {
    private String title;
    private String content;
}
