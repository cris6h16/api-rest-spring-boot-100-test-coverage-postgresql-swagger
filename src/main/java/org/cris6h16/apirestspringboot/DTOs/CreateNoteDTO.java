package org.cris6h16.apirestspringboot.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class CreateNoteDTO {
    //    private Long id;
    @NotBlank(message = "Title is required") // not  null/only spaces
    private String title;
    private String content;
}
