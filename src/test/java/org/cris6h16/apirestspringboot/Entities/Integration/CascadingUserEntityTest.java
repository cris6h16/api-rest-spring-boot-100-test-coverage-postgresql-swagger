package org.cris6h16.apirestspringboot.Entities.Integration;

import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the right behavior of cascading on {@link UserEntity} to the contained Entities
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional(rollbackFor = Exception.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CascadingUserEntityTest {

    @Autowired
    private UserRepository userRepository;
    private UserEntity usr;
    private RoleEntity role;
    private Set<NoteEntity> notes;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private NoteRepository noteRepository;


    /**
     * Before each test, delete all data from the repositories and
     * call {@link #initializeAndPrepare()};
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @BeforeEach
    void setUp() {

        userRepository.deleteAll();
        roleRepository.deleteAll();
        noteRepository.deleteAll();

        userRepository.flush();
        roleRepository.flush();
        noteRepository.flush();

        initializeAndPrepare();
    }

    /**
     * Initialize the {@code notes}, {@code role} and {@code usr} attributes,
     * It'll be used to avoid code repetition in the tests for initializations.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private void initializeAndPrepare() {
        notes = new HashSet<>(10);

        role = RoleEntity.builder()
                .id(null)
                .name(ERole.ROLE_USER)
                .build();

        usr = UserEntity.builder()
                .id(null)
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .notes(new HashSet<>())
                .build();

        for (int i = 0; i < 10; i++) {
            NoteEntity note = NoteEntity.builder()
                    .id(null)
                    .title("cris6h16's note title" + i)
                    .content("cris6h16's note content")
                    .build();
            notes.add(note);
        }
    }


    /**
     * Test the right behavior of cascading on {@link UserEntity} to the contained {@link RoleEntity}
     * <br>
     * If we have a not persisted {@link RoleEntity} in a {@link UserEntity} attribute, both
     * are not persisted ( both id = null )
     * <br>
     * When we persist the {@link UserEntity}, the {@link RoleEntity} is persisted too.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(1)
    @Tag("RoleEntity")
    public void CascadingUserEntity_WhenPersist_RoleEntity_Cascade() {
        // Arrange
        usr.setRoles(Set.of(role));
        Long usrId, roleId;

        // Act
        userRepository.saveAndFlush(usr);

        // Assert
        usrId = usr.getId();
        roleId = usr.getRoles().iterator().next().getId();
        assertThat(usrId).isNotNull();
        assertThat(roleId).isNotNull();
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(roleRepository.count()).isEqualTo(1);
    }

    /**
     * Test the right behavior of cascading on {@link UserEntity} to
     * the contained {@link RoleEntity}
     * <br>
     * If we have a persisted {@link RoleEntity} in a {@link UserEntity} attribute (both
     * are persisted ).
     * <br>
     * When we remove a {@link UserEntity}, the {@link RoleEntity} wouldn't
     * be removed, just the {@link UserEntity}.
     */
    @Test
    @Order(3)
    @Tag("RoleEntity")
    public void CascadingUserEntity_WhenRemove_RoleEntity_NotCascade() {
        // Arrange
        usr.setRoles(Set.of(role));
        userRepository.saveAndFlush(usr);
        boolean bothSaved = userRepository.count() == 1 && roleRepository.count() == 1;

        // Act
        userRepository.delete(usr);

        // Assert
        assertThat(bothSaved).isTrue();
        assertThat(userRepository.count()).isEqualTo(0);
        assertThat(roleRepository.count()).isEqualTo(1);
    }


    /**
     * Test the right behavior of cascading on {@link UserEntity}
     * to the contained {@link NoteEntity}
     * <br>
     * If we have a not persisted {@link NoteEntity} in a {@link UserEntity} attribute
     * (both are not persisted).
     * <br>
     * When we persist the {@link UserEntity}, the {@link NoteEntity} is persisted too.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(2)
    @Tag("NoteEntity")
    public void CascadingUserEntity_WhenPersist_NoteEntity_Cascade() {
        // Arrange
        usr.getNotes().addAll(notes);

        // Act
        userRepository.saveAndFlush(usr);

        // Assert
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(noteRepository.count()).isEqualTo(notes.size());
        assertThat(noteRepository.findByUser(usr).toArray())
                .containsAll(notes);
    }

    /**
     * Test the right behavior of cascading on {@link UserEntity}
     * to the contained {@link NoteEntity}
     * <br>
     * If we have a list of persisted {@link NoteEntity} in a {@link UserEntity} attribute
     * (both are persisted).
     * <br>
     * When we remove a {@link UserEntity}, all the {@link NoteEntity} would be removed too.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(4)
    @Tag("NoteEntity")
    public void CascadingUserEntity_WhenRemove_NoteEntity_Cascade() {
        // Arrange
        usr.getNotes().addAll(notes);
        userRepository.saveAndFlush(usr);
        boolean saved = userRepository.count() == 1 && noteRepository.count() == notes.size();

        // Act
        userRepository.delete(usr);

        // Assert
        assertThat(saved).isTrue();
        assertThat(userRepository.count()).isEqualTo(0);
        assertThat(noteRepository.count()).isEqualTo(0);
    }

}