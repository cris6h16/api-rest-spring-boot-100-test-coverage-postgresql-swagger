package org.cris6h16.apirestspringboot.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "username_unique", columnNames = "username"),
        @UniqueConstraint(name = "email_unique", columnNames = "email")
})
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

    @Column(name = "username", length = 20) //TODO: test passing a greater that 20
    @NotBlank(message = "Username mustn't be blank") // for sending null/empty
    private String username;


    @Column(name = "password")
    @NotBlank(message = "Password is required")
//    @Length(min = 8, message = "Password must be at least 8 characters") --> we handled it directly in @Service
    private String password;

    @Column(name = "email")
    @Email(message = "Email is invalid")// null is valid
    @NotBlank(message = "Email is required")
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

    //TODOl correct because we need impl soft deletes


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
