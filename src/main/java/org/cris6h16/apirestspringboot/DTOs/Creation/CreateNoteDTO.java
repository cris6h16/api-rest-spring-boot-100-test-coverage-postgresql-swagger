package org.cris6h16.apirestspringboot.DTOs.Creation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.cris6h16.apirestspringboot.DTOs.Interfaces.Notes.NullAttributesBlanker;

import static org.cris6h16.apirestspringboot.Constants.Cons.Note.Validations.*;


/**
 * DTO to create or update a note.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CreateNoteDTO implements NullAttributesBlanker {
//  Verification was centralized in the service layer( and its message), and verified manually to avoid increase the testing complexity(I don't use the validator bean)
//    @NotBlank(message = TITLE_IS_BLANK_MSG)
//    @NotNull(message = CONTENT_IS_NULL_MSG)

    private String title;
    private String content;

    @Override
    public void toBlankNullAttributes() {
        if (title == null) title = "";
        if (content == null) content = "";
    }
}
