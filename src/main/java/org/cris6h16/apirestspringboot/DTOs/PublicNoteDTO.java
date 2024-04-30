package org.cris6h16.apirestspringboot.DTOs;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class PublicNoteDTO {
    private String title;
    private String content;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;
}
