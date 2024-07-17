package org.cris6h16.apirestspringboot.Entities;

import org.cris6h16.apirestspringboot.Repositories.NoteRepository;
import org.cris6h16.apirestspringboot.Repositories.RoleRepository;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the right behavior of cascading on {@link UserEntity} to the contained Entities
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
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
     * Before each test, deleteByIdAndUserId all data from the repositories and
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
     * Initialize the {@link  #notes}, {@link #role} and {@link #usr} attributes,
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
                .createdAt(new Date())
                .build();

        for (int i = 0; i < 10; i++) {
            NoteEntity note = NoteEntity.builder()
                    .id(null)
                    .title("cris6h16's note title" + i)
                    .content("cris6h16's note content")
                    .updatedAt(new Date())
                    .build();
            notes.add(note);
        }
    }


    @Test
    @Order(1)
    @Tag("RoleEntity")
    public void WhenPersist_ContainedRoleEntitiesInCascade() {
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


    @Test
    @Order(3)
    @Tag("RoleEntity")
    public void WhenRemove_ContainedRoleEntitiesNotCascade() {
        // Arrange
        usr.setRoles(Set.of(role));
        userRepository.saveAndFlush(usr);
        boolean bothSaved = userRepository.count() == 1 && roleRepository.count() == 1;

        // Act
        userRepository.deleteById(usr.getId());

        // Assert
        assertThat(bothSaved).isTrue();
        assertThat(userRepository.count()).isEqualTo(0);
        assertThat(roleRepository.count()).isEqualTo(1);
    }


    @Test
    @Order(2)
    @Tag("NoteEntity")
    public void WhenPersist_ContainedNoteEntitiesCascade() {
        // Arrange
        usr.getNotes().addAll(notes);

        // Act
        userRepository.saveAndFlush(usr);

        // Assert
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(noteRepository.count()).isEqualTo(notes.size());
        assertThat(
                noteRepository.findByUserId(usr.getId(), Pageable.unpaged()).stream().toArray()
        ).containsAll(notes);
    }


    @Test
    @Order(4)
    @Tag("NoteEntity")
    public void WhenRemove_ContainedNoteEntitiesCascade() {
        // Arrange
        usr.getNotes().addAll(notes);
        userRepository.saveAndFlush(usr);
        boolean saved = userRepository.count() == 1 && noteRepository.count() == notes.size();

        // Act
        userRepository.deleteById(usr.getId());

        // Assert
        assertThat(saved).isTrue();
        assertThat(userRepository.count()).isEqualTo(0);
        assertThat(noteRepository.count()).isEqualTo(0);
    }

}