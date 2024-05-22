package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    private UserEntity usr;

    public UserRepositoryTest() {
        usr = UserEntity.builder()
                .id(null)
                .username("testUser1")
                .password("12345678")
                .email("test2@example.com")
                .roles(Set.of(RoleEntity.builder().name(ERole.ROLE_ADMIN).build())) // CASCADE.PERSIST for RoleEntity
                .build();
    }

    @BeforeEach
    void setUp() {
        if (userRepository.findByUsername(usr.getUsername()).isPresent()) return;
        userRepository.save(usr);
    }

    @Test
    @Order(1)
    void UserRepository_findByUsername_returnANonemptyOptional() {
        // Arrange
        assertThat(userRepository.findByEmail(usr.getEmail())).isPresent();

        // Act
        Optional<UserEntity> result = userRepository.findByUsername(usr.getUsername());

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get().getId()).isNotNull();
        assertThat(result.get().getUsername()).isEqualTo(usr.getUsername());
    }

    @Test
    @Order(2)
    void UserRepository_findByEmail_returnANonemptyOptional() {
        // Arrange
        assertThat(userRepository.findByEmail(usr.getEmail())).isPresent();

        // Act
        Optional<UserEntity> result = userRepository.findByEmail(usr.getEmail());

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get().getId()).isNotNull();
        assertThat(result.get().getEmail()).isEqualTo(usr.getEmail());
    }

    @Test
    @Order(3)
    void UserRepository_executeInTransaction_returnTrue() {
        // Arrange
        assertThat(userRepository.existsById(usr.getId())).isTrue();

        // Act
        boolean completed = userRepository.executeInTransaction(() -> {
            userRepository.delete(usr);
        });

        // Assert
        assertThat(completed).isTrue();
        assertThat(userRepository.existsById(usr.getId())).isFalse();
    }
}