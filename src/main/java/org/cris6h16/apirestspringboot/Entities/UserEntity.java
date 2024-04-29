package org.cris6h16.apirestspringboot.Entities;

import jakarta.persistence.*;
import jakarta.validation.Constraint;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "username_unique", columnNames = "username")
})
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default")
    @SequenceGenerator(name = "default", sequenceName = "id_user_seq", allocationSize = 50, initialValue = 1)
    private Long id;

    @Column(name = "username", nullable = false, length = 20)
    @Length(min = 1, max = 20, message = "Username must be between 1 and 20 characters")
    private String username;

    @Column(name = "password", nullable = false)
    @Length(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date updatedAt;

    @Column(name = "deleted_at", nullable = true)
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
    Set<RoleEntity> roles;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "note_id", foreignKey = @ForeignKey(name = "fk_note_id"))
    Set<NoteEntity> notes;


    // TODO: fix  =>  Single Responsibility Principle is violated here.
    @PostPersist
    public void prePersist() {
        createdAt = new Date(System.currentTimeMillis());
    }

    @PostUpdate
    public void preUpdate() {
        updatedAt = new Date(System.currentTimeMillis());
    }

    @PostRemove
    public void preRemove() {
        deletedAt = new Date(System.currentTimeMillis());
    }

}
