package org.cris6h16.apirestspringboot.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

// import static Validation responses
import static org.cris6h16.apirestspringboot.Constants.Cons.Note.Validations.*;

@Entity
@Table(name = "notes")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"})
@Getter
@Setter
@Builder
public class NoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default")
    @SequenceGenerator(name = "default", sequenceName = "id_note_seq", allocationSize = 50, initialValue = 1)
    private Long id;

    @Column(name = "title", length = MAX_TITLE_LENGTH)
    @NotBlank(message = TITLE_IS_BLANK_MSG) // not  null/only spaces
    @Length(max = 255, message = TITLE_MAX_LENGTH_MSG)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", updatable = false) // is a @PrePersist
    @NotNull(message = "@PrePersist: createdAt, is required") // user can't set it -> We sanitize it in @ControllerAdvice
    @Temporal(TemporalType.DATE)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.DATE)
    private Date updatedAt;

    @Column(name = "deleted_at")
    @Temporal(TemporalType.DATE)
    private Date deletedAt;



    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH) // remember @TEST that delete the Many shouldn't delete the One
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_Notes_user_id"))
    private UserEntity user;

    // TODO: improve -> single responsibility principle
    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
    }
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = new Date(System.currentTimeMillis());
    }
//    @PreRemove --> avoid for soft delete
//    public void preRemove() {
//        this.deletedAt = new Date(System.currentTimeMillis());
//    }


}
