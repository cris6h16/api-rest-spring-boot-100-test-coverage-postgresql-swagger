package org.cris6h16.apirestspringboot.DTOs.Creation;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Service.Interfaces.NoteService;
import org.hibernate.validator.constraints.Length;

import static org.cris6h16.apirestspringboot.Constants.Cons.Note.Validations.*;


/**
 * DTO for {@link NoteEntity}
 * <p>
 * - Used for request a creation through the {@link NoteService#create(CreateNoteDTO, Long)} <br>
 * - Also used to update through the {@link NoteService#put(Long, CreateNoteDTO, Long)}
 * </p>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateNoteDTO {
    @NotBlank(message = TITLE_IS_BLANK_MSG) // not  null/only spaces
    @Size(max = 255, message = TITLE_MAX_LENGTH_MSG)
    private String title;

    private String content;

}
