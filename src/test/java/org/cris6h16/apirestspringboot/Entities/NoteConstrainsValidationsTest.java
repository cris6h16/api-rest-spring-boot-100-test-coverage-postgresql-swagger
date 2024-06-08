package org.cris6h16.apirestspringboot.Entities;


import jakarta.validation.ConstraintViolationException;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
/**
 * Test class for {@link NoteEntity} validations and constraints<br>
 *
 * @author <a href="github.com/cris6h16" target="_blank">Cristian Herrera</a>
 */
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional(rollbackFor = Exception.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NoteConstrainsValidationsTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private NoteRepository noteRepository;
    private UserEntity usr;
    private NoteEntity note;

    /**
     * Before each test, delete all data from the repositories and
     * call {@link #_initializeAndPrepare()};
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        noteRepository.deleteAll();

        // necessary for h2
        userRepository.flush();
        roleRepository.flush();
        noteRepository.flush();

        //
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
    @Tag("correct")
    void NoteConstrainsValidationsTest_correctInsertion() {
        // Arrange
        userRepository.saveAndFlush(usr);

        // Act
        noteRepository.saveAndFlush(note);

        // Assert
        assertThat(noteRepository.findAll()).hasSize(1);
        assertThat(noteRepository.findAll().get(0)).isEqualTo(note);
    }

    /**
     * Test for {@link ConstraintViolationException} -> {@code title} is blank.
     * <br>
     * it violates {@code @NotBlank(message = <>)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(2)
    @Tag("ConstraintViolationException")
    void NoteConstrainsValidationsTest_ConstraintViolationException_titleIsBlank() {
        // Arrange
        userRepository.saveAndFlush(usr);
        note.setTitle(" ");

        // Act && Assert
        assertThrows(ConstraintViolationException.class, () -> noteRepository.saveAndFlush(note));
    }

    /**
     * Test for {@link ConstraintViolationException} -> {@code title} is null.
     * <br>
     * it violates {@code @NotBlank(message = <>)}
     *
     * @since 1.0
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     */
    @Test
    @Order(3)
    @Tag("ConstraintViolationException")
    void NoteConstrainsValidationsTest_ConstraintViolationException_titleIsNull() {
        // Arrange
        userRepository.saveAndFlush(usr);
        note.setTitle(null);

        // Act && Assert
        assertThrows(ConstraintViolationException.class, () -> noteRepository.saveAndFlush(note));
    }

    /**
     * Test for {@link ConstraintViolationException} -> {@code title} is too long.
     * <br>
     * it violates {@code @Length(max = <>, message = <>)}
     *
     * @since 1.0
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     */
    @Test
    @Order(4)
    @Tag("ConstraintViolationException")
    void NoteConstrainsValidationsTest_ConstraintViolationException_titleTooLong() {
        // Arrange
        userRepository.saveAndFlush(usr);
        note.setTitle("a".repeat(Cons.Note.Validations.MAX_TITLE_LENGTH + 1));

        // Act && Assert
        assertThrows(ConstraintViolationException.class, () -> noteRepository.saveAndFlush(note));
    }


    /**
     * Initialize the {@code usr} and {@code note} attributes,
     * it'll use to avoid code repetition of initialization
     * of this in each method.
     *
     * @since 1.0
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
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
        note = NoteEntity.builder()
                .id(null)
                .title("the first cris6h16's note")
                .content("the content of the first cris6h16's note")
                .build();
    }
}
