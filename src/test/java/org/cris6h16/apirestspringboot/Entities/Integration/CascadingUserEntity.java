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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

//@DataJpaTest
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
//@Transactional(rollbackFor = Exception.class)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class CascadingUserEntity {

    @Autowired
    private UserRepository userRepository;
    private UserEntity usr;
    private RoleEntity role;
    private Set<NoteEntity> notes;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private NoteRepository noteRepository;


    @BeforeEach
    void setUp() {
        initializeAndPrepare();

        userRepository.deleteAll();
        roleRepository.deleteAll();
        noteRepository.deleteAll();

        userRepository.flush();
        roleRepository.flush();
        noteRepository.flush();
    }


    private void initializeAndPrepare() {
        notes = new HashSet<>();

        role = RoleEntity.builder()
                .id(null)
                .name(ERole.ROLE_USER)
                .build();
        usr = UserEntity.builder()
                .id(null)
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
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

    @Test
    @Order(2)
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


    @Test
    @Order(3)
    @Tag("NoteEntity")
    public void CascadingUserEntity_WhenPersist_NoteEntity_Cascade() {
        // Arrange
        usr.setNotes(notes);

        // Act
        userRepository.saveAndFlush(usr);

        // Assert
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(noteRepository.count()).isEqualTo(notes.size());
        assertThat(noteRepository.findAll().stream()
                .allMatch(n -> n.getUser().getId().equals(usr.getId())))
                .isTrue();
    }

    @Test
    @Order(4)
    @Tag("NoteEntity")
    public void CascadingUserEntity_WhenRemove_NoteEntity_Cascade() {
        // Arrange
        usr.setNotes(notes);
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