package org.cris6h16.apirestspringboot.DTOs;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

@AllArgsConstructor
@ToString
@Getter
@Builder
public class PublicNoteDTO {
    private Long id;
    private String title;
    private String content;
    private Date updatedAt;
}
