package org.cris6h16.apirestspringboot.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.Date;
import java.util.Set;

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
@EqualsAndHashCode // take in mind the LAZYs, Try to compare with EAGER fetches
// TODO: doc about @EqualsAndHashCode must be set in all contained entities --> cased me a TROUBLE
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default")
    @SequenceGenerator(name = "default", sequenceName = "id_user_seq", allocationSize = 50, initialValue = 1)
    private Long id;

    @Column(name = "username", length = MAX_USERNAME_LENGTH) //TODO: test passing a greater that 20
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

    @Column(name = "deleted_at")
    @Temporal(TemporalType.DATE)
    private Date deletedAt;

    @ManyToMany(fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            targetEntity = RoleEntity.class)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            foreignKey = @ForeignKey(name = "fk_user_id"),
            inverseForeignKey = @ForeignKey(name = "fk_role_id")
    )
    private Set<RoleEntity> roles;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private Set<NoteEntity> notes;

    //TODO: correct because we need impl soft deletes


    // TODO: fix  =>  Single Responsibility Principle is violated here.
    @PrePersist
    public void prePersist() {
        createdAt = new Date(System.currentTimeMillis());
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = new Date(System.currentTimeMillis());
    }

//    @PostRemove  --> avoid for soft deletes
//    public void preRemove() {
//        deletedAt = new Date(System.currentTimeMillis());
//    }

}
