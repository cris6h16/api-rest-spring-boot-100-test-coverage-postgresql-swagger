package org.cris6h16.apirestspringboot.DTOs;

import lombok.*;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Service.Interfaces.NoteService;


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
@ToString
public class CreateNoteDTO {
    private String title;
    private String content;
}
