package org.cris6h16.apirestspringboot.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

@Entity
@Table(name = "notes")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class NoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default")
    @SequenceGenerator(name = "default", sequenceName = "id_note_seq", allocationSize = 50, initialValue = 1)
    private Long id;

    @Column(name = "title", nullable = false, columnDefinition = "VARCHAR(255)")
    @Length(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;

    @Column(name = "content", nullable = true, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date updatedAt;

    @Column(name = "deleted_at", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date deletedAt;

}
