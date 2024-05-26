package org.cris6h16.apirestspringboot.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.*;

// import static VALUES of VALIDATIONS and CONSTRAINTS
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Constrains.*;
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Validations.*;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = USERNAME_UNIQUE_NAME, columnNames = "username"),
                @UniqueConstraint(name = EMAIL_UNIQUE_NAME, columnNames = "email")}

        // `UNIQUE CONSTRAINT` elements, the indexes are created automatically
/*
indexes = {
            @Index(name = "users_username_idx", columnList = "username", unique = true),
            @Index(name = "users_email_idx", columnList = "email", unique = true)}
*/
)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"notes"})
@Getter
@Setter
//@EqualsAndHashCode(exclude = {"notes"})  // doesn't work, I spent a lot of time trying to make it work, even I excluded all, just works including only the `id`
@Builder
public class UserEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default")
    @SequenceGenerator(name = "default", sequenceName = "id_user_seq", allocationSize = 50, initialValue = 1)
    private Long id;

    @Column(name = "username", length = MAX_USERNAME_LENGTH)
    @Length(max = MAX_USERNAME_LENGTH, message = USERNAME_MAX_LENGTH_MSG)
    @NotBlank(message = USERNAME_IS_BLANK_MSG) // for sending null/empty
    private String username;


    @Column(name = "password")
    @NotBlank(message = PASS_IS_BLANK_MSG)
    // @Length(min = 8, message = "Password must be at least 8 characters") --> we handled it directly in @Service, min doesn't work very well, remember that is saved encrypted
    private String password;

    @Column(name = "email")
    @Email(message = EMAIL_INVALID_MSG)// --> null is valid
    @NotBlank(message = EMAIL_IS_BLANK_MSG)
    private String email;

    @Column(name = "created_at", nullable = true, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.DATE)
    private Date updatedAt;

    @ManyToMany(fetch = FetchType.EAGER,
            cascade = {CascadeType.PERSIST}, // i don't want to delete the roles when I delete a user, but I want to save the unsaved roles(id=null)
            targetEntity = RoleEntity.class)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            foreignKey = @ForeignKey(name = "fk_user_id"),
            inverseForeignKey = @ForeignKey(name = "fk_role_id")
    )
    private Set<RoleEntity> roles;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            targetEntity = NoteEntity.class,
            orphanRemoval = true)
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_notes_user_id"),
            referencedColumnName = "id")
    @Getter(AccessLevel.NONE) // avoid get all
    @Setter(AccessLevel.NONE) // the best would be to add not replace
    private Set<NoteEntity> notes = new HashSet<>();


    /**
     * todo: Make sure use with eager fetches<br>
     * Add notes to the user, if any note has an {@code id}, it will replace the existing note with the same {@code id}
     *
     * @param notes notes to add
     */
    public void putNoteEntities(NoteEntity... notes) {
        Arrays.stream(notes)
                .filter(n -> n.getId() != null)
                .forEach(passed -> this.notes.removeIf(owned -> owned.getId().equals(passed.getId())));
        this.notes.addAll(Set.of(notes));
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserEntity that = (UserEntity) obj;
        boolean isEquals = false;
        {
            boolean idEquals = Objects.equals(id, that.id);
            boolean usernameEquals = Objects.equals(username, that.username);
            boolean passwordEquals = Objects.equals(password, that.password);
            boolean emailEquals = Objects.equals(email, that.email);
            boolean createdAtEquals = Objects.equals(createdAt, that.createdAt);
            boolean updatedAtEquals = Objects.equals(updatedAt, that.updatedAt);
            boolean rolesEquals = Objects.equals(roles, that.roles);
            boolean notesEquals = Objects.equals(notes, that.notes);

            isEquals = idEquals &&
                    usernameEquals &&
                    emailEquals &&
                    rolesEquals &&
                    notesEquals &&
                    passwordEquals &&
                    createdAtEquals &&
                    updatedAtEquals;
        }

        return isEquals;
    }
}
