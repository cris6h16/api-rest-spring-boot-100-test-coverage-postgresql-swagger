package org.cris6h16.apirestspringboot.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.cris6h16.apirestspringboot.Constants.Cons.User.Constrains.EMAIL_UNIQUE_NAME;
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Constrains.USERNAME_UNIQUE_NAME;
import static org.cris6h16.apirestspringboot.Constants.Cons.User.Validations.*;

/**
 * Entity to represent the {@code users}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = USERNAME_UNIQUE_NAME, columnNames = "username"),
                @UniqueConstraint(name = EMAIL_UNIQUE_NAME, columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_" + USERNAME_UNIQUE_NAME, columnList = "username", unique = true),
                @Index(name = "idx_" + EMAIL_UNIQUE_NAME, columnList = "email", unique = true)
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default")
    @SequenceGenerator(name = "default", sequenceName = "id_user_seq", allocationSize = 50, initialValue = 1)
    private Long id;

    // DataIntegrityViolationException
    @Column(
            name = "username",
            nullable = false,
            columnDefinition = "VARCHAR(" + MAX_USERNAME_LENGTH + ") CHECK (LENGTH(username) >= " + MIN_USERNAME_LENGTH + ")"
            // unique = true ==> I can't put a custom name for the unique constraint
    )
    private String username;


    @Column(
            name = "password",
            nullable = false,
            columnDefinition = "VARCHAR(" + MAX_PASSWORD_LENGTH_ENCRYPTED + ") CHECK (LENGTH(password) >= " + MIN_PASSWORD_LENGTH + ")"
    )
    private String password;

    @Column(
            name = "email",
            nullable = false,
            columnDefinition = "VARCHAR(" + MAX_EMAIL_LENGTH + ") CHECK ( LENGTH(email) >= " + MIN_EMAIL_LENGTH + ")"
    )
    private String email;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false // UnsupportedOperationException
    )
    @Temporal(TemporalType.DATE)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.DATE)
    private Date updatedAt;

    @ManyToMany(fetch = FetchType.EAGER,
            cascade = {CascadeType.PERSIST},
            targetEntity = RoleEntity.class)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            foreignKey = @ForeignKey(name = "fk_user_id"),
            inverseForeignKey = @ForeignKey(name = "fk_role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            targetEntity = NoteEntity.class,
            orphanRemoval = true)
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_notes_user_id"),
            referencedColumnName = "id")
    private Set<NoteEntity> notes = new HashSet<>();

}


//@EqualsAndHashCode(exclude = {"notes"})  // doesn't work, I spent a lot of time trying to make it work, even I excluded all, just works including only the `id`
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) return true;
//        if (obj == null || getClass() != obj.getClass()) return false;
//        UserEntity that = (UserEntity) obj;
//        boolean isEquals = false;
//        {
//            boolean idEquals = Objects.equals(id, that.id);
//            boolean usernameEquals = Objects.equals(username, that.username);
//            boolean passwordEquals = Objects.equals(password, that.password);
//            boolean emailEquals = Objects.equals(email, that.email);
//            boolean createdAtEquals = Objects.equals(createdAt, that.createdAt);
//            boolean updatedAtEquals = Objects.equals(updatedAt, that.updatedAt);
//            boolean rolesEquals = Objects.equals(roles, that.roles);
//            boolean notesEquals = Objects.equals(notes, that.notes);
//
//            isEquals = idEquals &&
//                    usernameEquals &&
//                    emailEquals &&
//                    rolesEquals &&
//                    notesEquals &&
//                    passwordEquals &&
//                    createdAtEquals &&
//                    updatedAtEquals;
//        }
//
//        return isEquals;
//    }
