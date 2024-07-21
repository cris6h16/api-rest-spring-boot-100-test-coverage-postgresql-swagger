package org.cris6h16.apirestspringboot.Entities;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Repositories.RoleRepository;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for {@link UserEntity} validations and constraints<br>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
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


    @Test
    void DataIntegrityViolationException_usernameAlreadyExists() {
        // Arrange
        userRepository.saveAndFlush(usr);
        UserEntity usr2 = UserEntity.builder()
                .username(usr.getUsername())
                .email(usr.getEmail())
                .password("12345678")
                .roles(Set.of(roleRepository.findAll().iterator().next()))
                .createdAt(new Date())
                .build();
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(Cons.User.Constrains.USERNAME_UNIQUE_NAME);
    }


    @Test
    void DataIntegrityViolationException_emailAlreadyExists() {
        // Arrange
        userRepository.saveAndFlush(usr);
        UserEntity usr2 = UserEntity.builder()
                .username("github.com/cris6h16")
                .email(usr.getEmail())
                .password("12345678")
                .roles(Set.of(roleRepository.findAll().iterator().next()))
                .createdAt(new Date())
                .build();
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(Cons.User.Constrains.EMAIL_UNIQUE_NAME);
    }

    @Test
    void DataIntegrityViolationException_UsernameIsNull() {
        // Arrange
        usr.setUsername(null);
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("""
                        atement [ERROR: null value in column "username" of relation "users" violates not-null constraint
                        """);
    }

    @Test
    void DataIntegrityViolationException_UsernameTooShort() {
        // Arrange
        usr.setUsername("a".repeat(Cons.User.Validations.MIN_USERNAME_LENGTH - 1));
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("statement [ERROR: new row for relation \"users\" violates check constraint \"users_username_check\"");
    }

    @Test
    void DataIntegrityViolationException_UsernameTooLong() {
        // Arrange
        usr.setUsername("a".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH + 1));
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("could not execute statement [ERROR: value too long for type character varying(20)] [ins");
    }

    @Test
    void Successful_usernameColumn_usernameMinimumLength() {
        // Arrange
        usr.setUsername("a".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH));
        // Act & Assert
        userRepository.saveAndFlush(usr);
    }

    @Test
    void Successful_usernameColumn_usernameMaximumLength() {
        // Arrange
        usr.setUsername("a".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH));
        // Act & Assert
        userRepository.saveAndFlush(usr);
    }

    @Test
    void DataIntegrityViolationException_passwordIsNull() {
        // Arrange
        usr.setPassword(null);
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("could not execute statement [ERROR: null value in column \"password\" of relation \"users\" violates not-null constraint");
    }

    @Test
    void DataIntegrityViolationException_passwordTooShort() {
        // Arrange
        usr.setPassword("a".repeat(Cons.User.Validations.MIN_PASSWORD_LENGTH - 1));
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("could not execute statement [ERROR: new row for relation \"users\" violates check constraint \"users_password_check\"");
    }

    @Test
void DataIntegrityViolationException_passwordTooLong() {
        // Arrange
        usr.setPassword("a".repeat(Cons.User.Validations.MAX_PASSWORD_LENGTH_ENCRYPTED + 1));
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("could not execute statement [ERROR: value too long for type character varying(1000)] [in");
    }

    @Test
    void Successful_passwordColumn_passwordMinimumLength() {
        // Arrange
        usr.setPassword("a".repeat(Cons.User.Validations.MIN_PASSWORD_LENGTH));
        // Act & Assert
        userRepository.saveAndFlush(usr);
    }

    @Test
    void Successful_passwordColumn_passwordMaximumLength() {
        // Arrange
        usr.setPassword("a".repeat(Cons.User.Validations.MAX_PASSWORD_LENGTH_ENCRYPTED));
        // Act & Assert
        userRepository.saveAndFlush(usr);
    }

    @Test
    void DataIntegrityViolationException_emailIsNull() {
        // Arrange
        usr.setEmail(null);
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("could not execute statement [ERROR: null value in column \"email\" of relation \"users\" violates not-null constraint");
    }

    @Test
    void DataIntegrityViolationException_emailTooShort() {
        // Arrange
        usr.setEmail("a".repeat(Cons.User.Validations.MIN_EMAIL_LENGTH - 1));
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("could not execute statement [ERROR: new row for relation \"users\" violates check constraint \"users_email_check\"");
    }

    @Test
    void DataIntegrityViolationException_emailTooLong() {
        // Arrange
        usr.setEmail("a".repeat(Cons.User.Validations.MAX_EMAIL_LENGTH + 1));
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("could not execute statement [ERROR: value too long for type character varying(255)] [i");
    }

    @Test
    void Successful_emailColumn_emailMinimumLength() {
        // Arrange
        usr.setEmail("a".repeat(Cons.User.Validations.MIN_EMAIL_LENGTH));
        // Act & Assert
        userRepository.saveAndFlush(usr);
    }

    @Test
    void Successful_emailColumn_emailMaximumLength() {
        // Arrange
        usr.setEmail("a".repeat(Cons.User.Validations.MAX_EMAIL_LENGTH));
        // Act & Assert
        userRepository.saveAndFlush(usr);
    }


    @Test
    @Tag(value = "DataIntegrityViolationException")
    void DataIntegrityViolationException_createdAtColumn_isNull() {
        // Arrange
        usr.setCreatedAt(null);
        // Act & Assert
        assertThatThrownBy(() -> userRepository.saveAndFlush(usr))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("could not execute statement [ERROR: null value in column \"created_at\" of relation \"users\" violates not-null constraint");
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
