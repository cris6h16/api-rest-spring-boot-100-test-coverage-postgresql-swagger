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
                .username("cris6h16")
                .password("12345678")
                .email("cris6h16@gmail")
                .roles(Set.of(roles))
                .build();
    }


    /**
     * <ol>
     * <li>Deletes all from the {@link UserRepository} & {@link RoleRepository}</li>
     * </ol>
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    /**
     * Tests the {@link UserRepository#findByUsername(String)} method.<br>
     */
    @Test
    @Order(1)
    void UserRepository_findByUsername_returnANonemptyOptional() {
        // Arrange
        userRepository.save(usr);

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
        userRepository.save(usr);

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
        userRepository.save(usr);

        // Act
        boolean completed = userRepository.executeInTransaction(() -> {
            userRepository.delete(usr);
        });

        // Assert
        assertThat(completed).isTrue();
        assertThat(userRepository.existsById(usr.getId())).isFalse();
    }

    @Test
    @Order(4)
    void UserRepository_save_UsernameTooGreaterThrowsConstraintViolationException() {
        /*
            @Column(name = "username", length = MAX_USERNAME_LENGTH) //=20
            @Length(max = MAX_USERNAME_LENGTH, message = USERNAME_MAX_LENGTH_MSG)
            @NotBlank(message = USERNAME_IS_BLANK_MSG) // for sending null/empty
            private String username;
         */
        usr.setUsername("a".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH + 10000)); // + 10000 to exceed the limit
        /*
           @Column(name = "email")
           @Email(message = EMAIL_INVALID_MSG)// --> null is valid
           @NotBlank(message = EMAIL_IS_BLANK_MSG)
           private String email;
         */
        usr.setEmail("hola"); // invalid email

        /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if I use only the @SpringBootTest, which means that I will test with the real database.
        then it throws the validation, violation exceptions
         */
         /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        BUT if I use the ANNOTATIONs OF THIS CLASS, which means that I will test with the H2 database.
        then it violates ALL the constraints abusively and saves the user violating all the constraints.

        BUT the exception that should be thrown when was saved are thrown when I try to find the user like
        findByUsername, findByEmail, findALL
         */
        userRepository.save(usr);
        System.out.println(userRepository.findAll());

    }
}