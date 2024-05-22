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

/**
 * Test class for {@link UserRepository}.<br>
 * This class uses an embedded {@code H2} database to simulate the real database environment.<br>
 *
 * Using the {@code H2} database provides the following benefits:
 * <ul>
 *   <li>Isolation: Tests run in an isolated environment, ensuring no interference with the real database.</li>
 *   <li>Speed: Embedded databases like H2 execute faster than real databases, speeding up test execution.</li>
 *   <li>Maintenance: There is no need to clean the database manually, even if the database structure changes.</li>
 * </ul>
 *
 * Although you can configure tests to use the actual database, it is not recommended due to potential issues such as:
 * <ul>
 *   <li>Loss of isolation: Tests may interfere with real data, leading to inconsistent results.</li>
 *   <li>Slower execution: Real databases typically perform slower than in-memory databases like H2.</li>
 *   <li>Manual cleanup: Changes in the database structure may require manual cleanup, complicating test maintenance.</li>
 * </ul>
 *
 * @author  <a href="https://github.com/cris6h16" target="_blank">Cristian Herrera</a>
 */
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    private UserEntity usr;

    /**
     * Initializes the {@link UserEntity} object to be used in the tests.
     */
    public UserRepositoryTest() {
        RoleEntity roles = RoleEntity.builder().name(ERole.ROLE_ADMIN).build();
        usr = UserEntity.builder()
                .id(null)
                .username("testUser1")
                .password("12345678")
                .email("test2@example.com")
                .roles(Set.of(roles))
                .build();
    }


    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.saveAll(usr.getRoles());
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