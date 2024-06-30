package org.cris6h16.apirestspringboot.DTOs.Creation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Services.Interfaces.NoteService;

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
@Builder
public class CreateNoteDTO {

    @NotBlank(message = TITLE_IS_BLANK_MSG)
    private String title;

    @NotNull(message = CONTENT_IS_NULL_MSG)
    private String content;

}
