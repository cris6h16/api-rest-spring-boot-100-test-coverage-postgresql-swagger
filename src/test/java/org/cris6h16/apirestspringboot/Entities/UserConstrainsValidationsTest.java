package org.cris6h16.apirestspringboot.Entities;

import jakarta.validation.ConstraintViolationException;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Repositories.RoleRepository;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void correctInsertion() {
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
    @Tag(value = "DataIntegrityViolationException")
    void DataIntegrityViolationException_usernameAlreadyExists() {
        // Arrange
        userRepository.saveAndFlush(usr);
        UserEntity usr2 = UserEntity.builder()
                .username(usr.getUsername())
                .email(usr.getEmail())
                .password("12345678")
                .roles(Set.of(roleRepository.findAll().iterator().next()))
                .build();
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(Cons.User.Constrains.USERNAME_UNIQUE_NAME);
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
    @Tag(value = "DataIntegrityViolationException")
    void DataIntegrityViolationException_emailAlreadyExists() {
        // Arrange
        userRepository.saveAndFlush(usr);
        UserEntity usr2 = UserEntity.builder()
                .username("github.com/cris6h16")
                .email(usr.getEmail())
                .password("12345678")
                .roles(Set.of(roleRepository.findAll().iterator().next()))
                .build();
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(Cons.User.Constrains.EMAIL_UNIQUE_NAME);
    }

    @Test
    @Tag(value = "ConstraintViolationException")
    void ConstraintViolationException_usernameColumn_usernameTooLong() {
        // Arrange
        usr.setUsername("a".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH + 1));
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(Cons.User.Validations.USERNAME_MAX_LENGTH_MSG);
    }

    @Test
    void Successful_usernameColumn_usernameMinimumLength() {
        // Arrange
        usr.setUsername("a".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH));
        // Act & Assert
        userRepository.saveAndFlush(usr);
    }

//    Will never reached, because my jakarta validation is before the hibernate validation
//    @Test
//    @Tag(value = "DataIntegrityViolationException")
//    void DataIntegrityViolationException_usernameColumn_isNull() {
//        // Arrange
//        usr.setUsername(null);
//        // Act & Assert
//        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
//                .isInstanceOf(DataIntegrityViolationException.class)
//                .hasMessageContaining("some hibernate exception message");
//    }


    /**
     * Test for {@link ConstraintViolationException} -> Username too long
     * <br>
     * Username is too long, it violates {@code  @Length(max = <>, message = <>)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag(value = "ConstraintViolationException")
    void ConstraintViolationException_usernameTooLong() {
        // Arrange
        usr.setUsername("a".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH + 1));
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(Cons.User.Validations.USERNAME_MAX_LENGTH_MSG);
    }


    @ParameterizedTest
    @ValueSource(strings = {"blank", "empty", "null"})
    @Tag(value = "ConstraintViolationException")
    void ConstraintViolationException_usernameIsBlankOrEmptyOrNull(String username) {
        // Arrange
        username = switch (username) {
            case "blank" -> "             ";
            case "empty" -> "";
            case "null" -> null;
            default -> throw new IllegalArgumentException("unexpected value: " + username);
        };
        usr.setUsername(username);

        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(Cons.User.Validations.USERNAME_IS_BLANK_MSG);
    }

//   Will never reached, because my jakarta validation is before the hibernate validation
//    @Test
//    @Tag(value = "DataIntegrityViolationException")
//    void DataIntegrityViolationException_passwordColumn_isNull() {
//        // Arrange
//        usr.setPassword(null);
//        // Act & Assert
//        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
//                .isInstanceOf(DataIntegrityViolationException.class)
//                .hasMessageContaining("some hibernate exception message");
//    }

    @Test
    @Tag(value = "ConstraintViolationException")
    void ConstraintViolationException_passwordColumn_LetAtLeast300Characters() { // definition should be text
        // Arrange
        usr.setPassword("a".repeat(300));
        // Act & Assert
        userRepository.saveAndFlush(usr);
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "blank", "empty", "tooShort"})
    @Tag(value = "ConstraintViolationException")
    void ConstraintViolationException_passwordIsNullOrBlankOrEmptyOrTooShort(String password) {
        // Arrange
        password = switch (password) {
            case "null" -> null;
            case "blank" -> "             ";
            case "empty" -> "";
            case "tooShort" -> "1".repeat(Cons.User.Validations.MIN_PASSWORD_LENGTH - 1);
            default -> throw new IllegalArgumentException("unexpected value: " + password);
        };
        usr.setPassword(null);

        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(Cons.User.Validations.InService.PASS_IS_TOO_SHORT_MSG);
    }

//   Will never reached, because my jakarta validation is before the hibernate validation
//    @Test
//    @Tag(value = "DataIntegrityViolationException")
//    void DataIntegrityViolationException_emailColumn_isNull() {
//        // Arrange
//        usr.setEmail(null);
//        // Act & Assert
//        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
//                .isInstanceOf(DataIntegrityViolationException.class)
//                .hasMessageContaining("some hibernate exception message");
//    }




    @Test
    @Tag(value = "ConstraintViolationException")
    void ConstraintViolationException_emailInvalid() {
        // Arrange
        usr.setEmail("cris6h16");
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(Cons.User.Validations.EMAIL_INVALID_MSG);
    }

    /**
     * Test for {@link ConstraintViolationException} -> Email is null
     * <br>
     * Email is null, it violates {@code  @NotBlank(message = <>)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Tag(value = "ConstraintViolationException")
    @ParameterizedTest
    @ValueSource(strings = {"null", "blank", "empty"})
    void ConstraintViolationException_emailIsNullOrBlankOrEmpty(String email) {
        // Arrange
        email = switch (email) {
            case "null" -> null;
            case "blank" -> "             ";
            case "empty" -> "";
            default -> throw new IllegalArgumentException("unexpected value: " + email);
        };
        usr.setEmail(email);

        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(Cons.User.Validations.EMAIL_IS_BLANK_MSG);
    }


    @Test
    @Tag(value = "DataIntegrityViolationException")
    void DataIntegrityViolationException_createdAtColumn_isNull() {
        // Arrange
        usr.setCreatedAt(null);
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("column \"CREATED_AT\"");
    }

    @Test
    @Tag(value = "UnsupportedOperationException")
    void UnsupportedOperationException_createdAtColumn_isNotUpdatable() {
        // Arrange
        usr.setCreatedAt(new Date());

        // Act & Assert
        userRepository.saveAndFlush(usr);
        usr.setCreatedAt(new Date());
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @Tag(value = "ConstraintViolationException")
    /**
     * Initialize and prepare the {@link #usr} attribute. it's used
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
                .createdAt(new Date())
                .build();
    }
}
