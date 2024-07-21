package org.cris6h16.apirestspringboot.Entities;


import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Repositories.NoteRepository;
import org.cris6h16.apirestspringboot.Repositories.RoleRepository;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
/**
 * Test class for {@link NoteEntity} validations and constraints<br>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
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
    void correctInsertion() {
        // Arrange
        userRepository.saveAndFlush(usr);

        // Act
        noteRepository.saveAndFlush(note);

        // Assert
        assertThat(noteRepository.findAll()).hasSize(1);
        assertThat(noteRepository.findAll().get(0)).isEqualTo(note);
    }

    @Test
    void DataIntegrityViolationException_titleColumnIsTooLong() {
        // Arrange
        userRepository.saveAndFlush(usr);
        note.setTitle("a".repeat(Cons.Note.Validations.MAX_TITLE_LENGTH + 1));

        // Act && Assert
        assertThatThrownBy(() -> noteRepository.saveAndFlush(note))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("could not execute statement [ERROR: value too long for type character varying(255)] [insert int");
    }

    @Test
    @Tag("success")
    void success_titleColumnLength_isTheLimit() {
        // Arrange
        userRepository.saveAndFlush(usr);
        note.setTitle("a".repeat(Cons.Note.Validations.MAX_TITLE_LENGTH));

        // Act
        noteRepository.saveAndFlush(note);

        // Assert
        assertThat(noteRepository.findAll()).hasSize(1);
        assertThat(noteRepository.findAll().get(0)).isEqualTo(note);
    }
    @Test
    void DataIntegrityViolationException_titleColumnIsNull() {
        // Arrange
        note.setTitle(null);

        // Act && Assert
        assertThatThrownBy(() -> noteRepository.saveAndFlush(note))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("could not execute statement [ERROR: null value in column \"title\" of relation \"notes\" violates not-null constraint");
    }


    @Test
    void Success_contentColumnLength_AtLeast500Chars() {
        // Arrange
        userRepository.saveAndFlush(usr);
        note.setContent("a".repeat(500));

        // Act
        noteRepository.saveAndFlush(note);

        // Assert
        assertThat(noteRepository.findAll()).hasSize(1);
        assertThat(noteRepository.findAll().get(0)).isEqualTo(note);
    }

    @Test
    void DataIntegrityViolationException_contentColumnIsNull() {
        // Arrange
        userRepository.saveAndFlush(usr);
        note.setContent(null);

        // Act && Assert
        assertThatThrownBy(() -> noteRepository.saveAndFlush(note))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("could not execute statement [ERROR: null value in column \"content\" of relation \"notes\" violates not-null constraint");
    }


    @Test
    void DataIntegrityViolationException_updateAtColumnIsNull() {
        // Arrange
        userRepository.saveAndFlush(usr);
        note.setUpdatedAt(null);

        // Act && Assert
        assertThatThrownBy(() -> noteRepository.saveAndFlush(note))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("null value in column \"updated_at\" of relation \"notes\" violates not-null constraint");
    }
    /**
     * Initialize the {@link  #usr} and {@link  #note} attributes,
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
                .createdAt(new Date())
                .build();
        note = NoteEntity.builder()
                .id(null)
                .title("the first cris6h16's note")
                .content("the content of the first cris6h16's note")
                .updatedAt(new Date())
                .build();
    }
}
