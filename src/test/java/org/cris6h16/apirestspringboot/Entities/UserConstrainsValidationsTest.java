package org.cris6h16.apirestspringboot.Entities;

import jakarta.validation.ConstraintViolationException;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link UserEntity} validations and constraints<br>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional(rollbackFor = Exception.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserConstrainsValidationsTest {
    //    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    private UserEntity usr;

    /**
     * Before each test, deleteById all data from the repositories and
     * call {@link #_initializeAndPrepare()};
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // necessary with h2
        userRepository.flush();
        roleRepository.flush();

        // `usr`
        _initializeAndPrepare();
    }

    /**
     * Test for correct insertion in the database, this
     * should be the first test to run, since it doesn't
     * violate any constraint/validations. We can continue
     * with the other tests if this one green.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(1)
    @Tag(value = "correct")
    void UserConstrainsValidationsTest_correctInsertion() {
        // Arrange
        // Act
        userRepository.saveAndFlush(usr);
        // Assert
        Iterator<?> i = userRepository.findAll().iterator();
        assertThat(i.hasNext()).isTrue();
        assertThat(i.next().equals(usr)).isTrue();
    }

    /**
     * Test for {@link DataIntegrityViolationException} -> Unique constraint violation
     * <br>
     * Username already exists in the database then we try one with the same username
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(2)
    @Tag(value = "DataIntegrityViolationException")
    void UserConstrainsValidationsTest_DataIntegrityViolationException_usernameAlreadyExists() {
        // Arrange
        userRepository.saveAndFlush(usr);
        UserEntity usr2 = UserEntity.builder()
                .username(usr.getUsername())
                .email(usr.getEmail())
                .password("12345678")
                .roles(Set.of(roleRepository.findAll().iterator().next()))
                .build();
        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(usr2));
    }

    /**
     * Test for {@link DataIntegrityViolationException} -> Unique constraint violation
     * <br>
     * Email already exists in the database then we try one with the same email
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(3)
    @Tag(value = "DataIntegrityViolationException")
    void UserConstrainsValidationsTest_DataIntegrityViolationException_emailAlreadyExists() {
        // Arrange
        userRepository.saveAndFlush(usr);
        UserEntity usr2 = UserEntity.builder()
                .username("github.com/cris6h16")
                .email(usr.getEmail())
                .password("12345678")
                .roles(Set.of(roleRepository.findAll().iterator().next()))
                .build();
        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(usr2));
    }


    /**
     * Test for {@link ConstraintViolationException} -> Username too long
     * <br>
     * Username is too long, it violates {@code  @Length(max = <>, message = <>)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(4)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_usernameTooLong() {
        // Arrange
        usr.setUsername("a".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH + 1));
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }


    /**
     * Test for {@link ConstraintViolationException} -> Username is blank
     * <br>
     * Username is blank, it violates {@code  @NotBlank(message = <>)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(5)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_usernameIsBlank() {
        // Arrange
        usr.setUsername("             ");
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }

    /**
     * Test for {@link ConstraintViolationException} -> Username is null
     * <br>
     * Username is null, it violates {@code  @NotBlank(message = <>)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(6)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_usernameIsNull() {
        // Arrange
        usr.setUsername(null);
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }


    /**
     * Test for {@link ConstraintViolationException} -> Password is blank
     * <br>
     * Password is blank, it violates {@code  @NotBlank(message = <>)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(7)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_passwordIsBlank() {
        // Arrange
        usr.setPassword("             ");
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }

    /**
     * Test for {@link ConstraintViolationException} -> Password is null
     * <br>
     * Password is null, it violates {@code  @NotBlank(message = <>)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(8)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_passwordIsNull() {
        // Arrange
        usr.setPassword(null);
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }

    /**
     * Test for {@link ConstraintViolationException} -> Email is invalid
     * <br>
     * Email is invalid, it violates {@code  @Email(message = <>)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(9)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_emailInvalid() {
        // Arrange
        usr.setEmail("cris6h16");
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }

    /**
     * Test for {@link ConstraintViolationException} -> Email is null
     * <br>
     * Email is null, it violates {@code  @NotBlank(message = <>)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(10)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_emailIsNull() {
        // Arrange
        usr.setEmail("cris6h16");
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }


    /**
     * Test for {@link ConstraintViolationException} -> Email is blank
     * <br>
     * Email is blank, it violates {@code  @NotBlank(message = <>)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(11)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_emailIsBlank() {
        // Arrange
        usr.setEmail("cris6h16");
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }


    /**
     * Initialize and prepare the {@code usr} attribute. it's used
     * in the tests, to avoid boilerplate initializations on each method.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private void _initializeAndPrepare() {
        RoleEntity roles = RoleEntity.builder().name(ERole.ROLE_USER).build();
        usr = UserEntity.builder()
                .id(null)
//                .username("cris6h16")
                .username("a".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH))
                .password("12345678")
                .email("cris6h16@gmail.com")
                .roles(Set.of(roles))
                .build();
    }
}
