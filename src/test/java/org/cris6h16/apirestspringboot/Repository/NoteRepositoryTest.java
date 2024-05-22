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


/**
 * Test class for {@link NoteRepository}<br>
 * This class uses an embedded {@code H2} database to simulate the real database environment.<br>
 *
 * Using the {@code H2} database provides the following benefits:
 * <ul>
 *   <li>Isolation: Tests run in an isolated environment, ensuring no interference with the real database.</li>
 *   <li>Speed: Embedded databases like H2 execute faster than real databases, speeding up test execution.</li>
 *   <li>Maintenance: There is no need to clean the database manually, even if the database structure changes.</li>
 * </ul>
 *
 * Although you can configure tests to use the actual database, it is not recommended due to potential issues such as:
 * <ul>
 *   <li>Loss of isolation: Tests may interfere with real data, leading to inconsistent results.</li>
 *   <li>Slower execution: Real databases typically perform slower than in-memory databases like H2.</li>
 *   <li>Manual cleanup: Changes in the database structure may require manual cleanup, complicating test maintenance.</li>
 * </ul>
 *
 * @author <a href="github.com/cris6h16" target="_blank">Cristian Herrera</a>
 */
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
public class NoteRepositoryTest {
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private UserRepository userRepository;

    private Map<UserEntity, List<NoteEntity>> userNotes;

    /**
     * Creates two users with 5 notes each and assigns them to the {@code userNotes} map.
     */
    public NoteRepositoryTest() {
        List<NoteEntity> n1 = assignAUser(createNotes(1, 5), createUser("cris6h16"));
        List<NoteEntity> n2 = assignAUser(createNotes(6, 10), createUser("cris6h16_2"));

        userNotes = new HashMap<>();
        UserEntity user1 = n1.get(0).getUser();
        UserEntity user2 = n2.get(0).getUser();
        userNotes.put(user1, n1);
        userNotes.put(user2, n2);
    }


    /**
     * <ol>
     *     <li>Deletes all from {@link UserRepository } & {@link NoteRepositoryTest}</li>
     *     <li>Saves all the users and notes from {@code userNotes} map</li>
     * </ol>
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        noteRepository.deleteAll();

        // save each separately to avoid cascades
        userRepository.saveAll(userNotes.keySet());
        noteRepository.saveAll(userNotes.values().stream().flatMap(noteList -> noteList.stream()).toList());
    }

    /**
     * Tests the {@link  NoteRepository#findByUserId(Long)} method.<br>
     * The method should return a list of notes(size=5) of each user.
     */
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

    /**
     * Tests the {@link NoteRepository#findByIdAndUserId(Long, Long)} method.<br>
     * Try to fetch each note(10) with each user(2), if the user is the owner of the note
     * then the method should return a non-empty {@code Optional} with the note, otherwise
     * it should return an empty {@code Optional}.
     */
    @Test
    void NoteRepository_findByIdAndUserId_returnANonemptyOptionalIfIsHisNote() {
        // Arrange
        assertThat(noteRepository.count()).isEqualTo(10);
        assertThat(userRepository.count()).isEqualTo(2);

        for (UserEntity usr : userNotes.keySet()) {
            for (NoteEntity n : userNotes.values().stream().flatMap(List::stream).toList()) {
                // Act
                Optional<NoteEntity> found = noteRepository.findByIdAndUserId(n.getId(), usr.getId());

                Long userId = usr.getId();
                Long userIdInNote = n.getUser().getId();
                // Assert
                if (userId.equals(userIdInNote)) {
                    assertThat(found).isPresent();
                    assertThat(found.get().getId()).isEqualTo(n.getId());
                    assertThat(found.get().getUser().getId()).isEqualTo(usr.getId());
                } else {
                    assertThat(found).isEmpty();
                }
            }
        }
    }

    /**
     * Tests the {@link  NoteRepository#findByUserId(Long, Pageable)} )} method <br>
     * The method should return the notes of the user in pages of 2 elements each.<br>
     * Here I get all the existent pages of notes of each user, the pages are sorted by
     * the {@code title} field in ascending order.
     *
     */
    @Test
    void NoteRepository_FindByUserIdPageable_returnPagesASC() {
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

    /**
     * Tests the {@link  NoteRepository#findByUserId(Long, Pageable)} )} method <br>
     * The method should return the notes of the user in pages of 2 elements each.<br>
     * Here I get all the existent pages of notes of each user, the pages are sorted by
     * the {@code title} field in descending order.
     *
     */
    @Test
    void NoteRepository_FindByUserIdPageable_returnPagesDES() {
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