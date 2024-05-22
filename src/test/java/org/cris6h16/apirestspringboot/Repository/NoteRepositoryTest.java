package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
public class NoteRepositoryTest {
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private UserRepository userRepository;

    private Map<UserEntity, List<NoteEntity>> userNotes;

    public NoteRepositoryTest() {
        List<NoteEntity> n1 = assignAUser(createNotes(1, 5), createUser("cris6h16"));
        List<NoteEntity> n2 = assignAUser(createNotes(6, 10), createUser("cris6h16_2"));

        userNotes = new HashMap<>();
        UserEntity user1 = n1.get(0).getUser();
        UserEntity user2 = n2.get(0).getUser();
        userNotes.put(user1, n1);
        userNotes.put(user2, n2);
    }


    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        noteRepository.deleteAll();

        // save each separately to avoid cascades
        userRepository.saveAll(userNotes.keySet());
        noteRepository.saveAll(userNotes.values().stream().flatMap(noteList -> noteList.stream()).toList());
    }

    @Test
    void NoteRepository_findByUserId_returnAListOfHisNotes() {
        // Arrange
        assertThat(noteRepository.count()).isEqualTo(10);
        assertThat(userRepository.count()).isEqualTo(2);

        for (UserEntity user : userNotes.keySet()) {
            // Act
            List<NoteEntity> notes = noteRepository.findByUserId(user.getId());

            // Assert
            assertThat(notes).hasSize(5);
            for (NoteEntity note : notes) {
                assertThat(note.getUser().getId()).isEqualTo(user.getId());
            }
        }
    }

    @Test
    void NoteRepository_findByIdAndUserId_returnANonemptyOptionalIfIsHisNote() {
        // Arrange
        assertThat(noteRepository.count()).isEqualTo(10);
        assertThat(userRepository.count()).isEqualTo(2);

        for (UserEntity user : userNotes.keySet()) {
            for (List<NoteEntity> notes : userNotes.values()) {
                for (NoteEntity n : notes) {
                    // Act
                    Optional<NoteEntity> found = noteRepository.findByIdAndUserId(n.getId(), user.getId());

                    Long userId = user.getId();
                    Long userIdInNote = n.getUser().getId();
                    // Assert
                    if (userId.equals(userIdInNote)) {
                        assertThat(found).isPresent();
                        assertThat(found.get().getId()).isEqualTo(n.getId());
                        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
                    } else {
                        assertThat(found).isEmpty();
                    }

                }
            }
        }
    }

    @Test
    void NoteRepository_testFindByUserIdPageableASC_returnPages() {
        // Arrange
        assertThat(noteRepository.count()).isEqualTo(10);
        assertThat(userRepository.count()).isEqualTo(2);
        byte pageSize = 2;
        byte pageNumber = 0;
        Sort sort = Sort.by(Sort.Order.asc("title"));
        byte[] expectedEachPageElements = new byte[]{2, 2, 1};

        byte expectedTotalPages = 3;
        byte expectedTotalElements = 5;

        for (Long userId : userNotes.keySet().stream().map(UserEntity::getId).toList()) {
            pageNumber = 0;

            for (byte thisPageSize : expectedEachPageElements) {
                // Act
                Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
                Page<NoteEntity> page = noteRepository.findByUserId(userId, pageable);

                // Assert
                assertThat(page).isNotNull();
                assertThat(page.getTotalPages()).isEqualTo(expectedTotalPages);
                assertThat(page.getTotalElements()).isEqualTo(expectedTotalElements);
                assertThat(page.getContent()).hasSize(thisPageSize);
                assertThat(page.getNumber()).isEqualTo(pageNumber);
                assertThat(page.getSize()).isEqualTo(pageSize);
                assertThat(page.stream().map(NoteEntity::getTitle))
                        .isSortedAccordingTo(Comparator.naturalOrder());

                if (++pageNumber >= page.getTotalPages())
                    break;
            }
        }
    }

    @Test
    void NoteRepository_testFindByUserIdPageableDES_returnPages() {
        // Arrange
        assertThat(noteRepository.count()).isEqualTo(10);
        assertThat(userRepository.count()).isEqualTo(2);
        byte pageSize = 2;
        byte pageNumber = 0;
        Sort sort = Sort.by(Sort.Order.desc("title"));
        byte[] expectedEachPageElements = new byte[]{2, 2, 1};

        byte expectedTotalPages = 3;
        byte expectedTotalElements = 5;

        for (Long userId : userNotes.keySet().stream().map(UserEntity::getId).toList()) {
            pageNumber = 0;

            for (byte thisPageSize : expectedEachPageElements) {
                // Act
                Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
                Page<NoteEntity> page = noteRepository.findByUserId(userId, pageable);

                // Assert
                assertThat(page).isNotNull();
                assertThat(page.getTotalPages()).isEqualTo(expectedTotalPages);
                assertThat(page.getTotalElements()).isEqualTo(expectedTotalElements);
                assertThat(page.getContent()).hasSize(thisPageSize);
                assertThat(page.getNumber()).isEqualTo(pageNumber);
                assertThat(page.getSize()).isEqualTo(pageSize);
                assertThat(page.stream().map(NoteEntity::getTitle))
                        .isSortedAccordingTo(Comparator.reverseOrder());

                if (++pageNumber >= page.getTotalPages())
                    break;
            }
        }
    }


    private UserEntity createUser(String username) {
        return UserEntity.builder()
                .username(username)
                .email(username + "@example.com")
                .password("12345678")
                .build();
    }

    private List<NoteEntity> createNotes(int startSuffix, int endSuffix) {
        List<NoteEntity> notes = new ArrayList<>();
        for (int i = startSuffix; i <= endSuffix; i++) {
            notes.add(NoteEntity.builder().title("title" + i).content("content" + i).build());
        }
        return notes;
    }

    private List<NoteEntity> assignAUser(List<NoteEntity> notes, UserEntity user) {
        notes.forEach(note -> note.setUser(user));
        return notes;
    }
}