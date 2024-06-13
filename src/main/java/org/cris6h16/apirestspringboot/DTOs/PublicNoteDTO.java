package org.cris6h16.apirestspringboot.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;

import java.util.Date;

/**
 * DTO for {@link NoteEntity} with relevant information for public access.<br>
 * comparing with the entity, this DTO hides the {@code user} field.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@AllArgsConstructor
@Getter
@Builder
public class PublicNoteDTO {
    private Long id;
    private String title;
    private String content;
    private Date updatedAt;
}
