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
 * @author <a href="github.com/cris6h16" target="_blank">Cristian Herrera</a>
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
     *
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // necessary with h2
        userRepository.flush();
        roleRepository.flush();

        // `usr`
        initializeAndPrepare();
    }

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


    @Test
    @Order(4)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_usernameTooLong() {
        // Arrange
        usr.setUsername("a".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH + 1));
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }


    @Test
    @Order(5)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_usernameIsBlank(){
        // Arrange
        usr.setUsername("             ");
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }

    @Test
    @Order(6)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_usernameIsNull(){
        // Arrange
        usr.setUsername(null);
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }


    @Test
    @Order(7)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_passwordIsBlank(){
        // Arrange
        usr.setPassword("             ");
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }

    @Test
    @Order(8)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_passwordIsNull(){
        // Arrange
        usr.setPassword(null);
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }

    @Test
    @Order(9)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_emailInvalid(){
        // Arrange
        usr.setEmail("cris6h16");
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }

    @Test
    @Order(10)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_emailIsNull(){
        // Arrange
        usr.setEmail("cris6h16");
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }


    @Test
    @Order(11)
    @Tag(value = "ConstraintViolationException")
    void UserConstrainsValidationsTest_ConstraintViolationException_emailIsBlank(){
        // Arrange
        usr.setEmail("cris6h16");
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userRepository.saveAndFlush(usr));
    }


    void initializeAndPrepare(){
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
