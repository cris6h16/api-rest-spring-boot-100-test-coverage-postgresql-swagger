package org.cris6h16.apirestspringboot.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

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
        uniqueConstraints = { // for `UNIQUE CONSTRAINT` elements, the indexes are created automatically
                @UniqueConstraint(name = USERNAME_UNIQUE_NAME, columnNames = "username"),
                @UniqueConstraint(name = EMAIL_UNIQUE_NAME, columnNames = "email")}
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

    @Column(name = "username", length = MAX_USERNAME_LENGTH)
    @Length(max = MAX_USERNAME_LENGTH, message = USERNAME_MAX_LENGTH_MSG)
    @NotBlank(message = USERNAME_IS_BLANK_MSG) // for sending null/empty
    private String username;


    @Column(name = "password", updatable = true)
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
