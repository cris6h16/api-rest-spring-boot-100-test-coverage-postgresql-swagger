package org.cris6h16.apirestspringboot.Repository;

import jakarta.validation.ConstraintViolationException;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link UserRepository}.<br>
 * This class uses an embedded {@code H2} database to simulate the real database environment.<br>
 * <p>
 * Using the {@code H2} database provides the following benefits:
 * <ul>
 *   <li>Isolation: Tests run in an isolated environment, ensuring no interference with the real database.</li>
 *   <li>Speed: Embedded databases like H2 execute faster than real databases, speeding up test execution.</li>
 *   <li>Maintenance: There is no need to clean the database manually, even if the database structure changes.</li>
 * </ul>
 * <p>
 * Although you can configure tests to use the actual database, it is not recommended due to potential issues such as:
 * <ul>
 *   <li>Loss of isolation: Tests may interfere with real data, leading to inconsistent results.</li>
 *   <li>Slower execution: Real databases typically perform slower than in-memory databases like H2.</li>
 *   <li>Manual cleanup: Changes in the database structure may require manual cleanup, complicating test maintenance.</li>
 * </ul>
 *
 * @author <a href="https://github.com/cris6h16" target="_blank">Cristian Herrera</a>
 */
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
@Transactional(rollbackFor = Exception.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryTest {

    //TODO: DOC MY TROUBLE WITH save() instead of saveAndFlush() in H2
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    private UserEntity usr;

    /**
     * Deletes all from the {@link UserRepository} & {@link RoleRepository}
     * and prepares a new entity to be created as {@code usr} (id==null)
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // necessary with H2
        userRepository.flush();
        roleRepository.flush();

        // `usr`
        initializeAndPrepare();
    }

    /**
     * Tests the {@link UserRepository#findByUsername(String)} method.<br>
     */
    @Test
    @Order(1)
    void UserRepository_findByUsername_returnANonemptyOptional() {
        // Arrange
        userRepository.saveAndFlush(usr);

        // Act
        Optional<UserEntity> result = userRepository.findByUsername(usr.getUsername());

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get().getId()).isNotNull();
        assertThat(result.get().getUsername()).isEqualTo(usr.getUsername());
    }

    /**
     * Tests the {@link UserRepository#findByEmail(String)} method.<br>
     */
    @Test
    @Order(2)
    void UserRepository_findByEmail_returnANonemptyOptional() {
        // Arrange
        userRepository.saveAndFlush(usr);

        // Act
        Optional<UserEntity> result = userRepository.findByEmail(usr.getEmail());

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get().getId()).isNotNull();
        assertThat(result.get().getEmail()).isEqualTo(usr.getEmail());
    }

    /**
     * Tests the {@link UserRepository#executeInTransaction(Runnable)} method.<br>
     */
    @Test
    @Order(3)
    void UserRepository_executeInTransaction_returnTrue() {
        // Arrange
        userRepository.saveAndFlush(usr);
        boolean completed = true;

        // Act
        try {
            userRepository.executeInTransaction(() -> {
                userRepository.delete(usr);
                userRepository.flush();
            });
        } catch (Exception e) {
            completed = false;
        }


        // Assert
        assertThat(completed).isTrue();
        assertThat(userRepository.existsById(usr.getId())).isFalse();

    }



    void initializeAndPrepare(){
        RoleEntity roles = RoleEntity.builder().name(ERole.ROLE_USER).build();
        usr = UserEntity.builder()
                .id(null)
                .username("cris6h16")
                .password("12345678")
                .email("cris6h16@gmail.com")
                .roles(Set.of(roles))
                .build();
    }

}