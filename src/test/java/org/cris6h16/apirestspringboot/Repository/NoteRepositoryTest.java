package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Test class for {@link NoteRepository}<br>
 * This class uses an embedded {@code H2} database to simulate the real database environment.<br>
 * <p>
 * Using the {@code H2} database provides the following benefits:
 * <ul>
 *   <li>Isolation: Tests run in an isolated environment, ensuring no interference with the real database.</li>
 *   <li>Speed: Embedded databases like H2 execute faster than real databases, speeding up test execution.</li>
 *   <li>Maintenance: There is no need to clean the database manually, even if the database structure changes.</li>
 * </ul>
 * <p>
 * Although you can configure tests to use the actual database, it is not recommended due to potential issues such as:
 * <ul>
 *   <li>Loss of isolation: Tests may interfere with real data, leading to inconsistent results.</li>
 *   <li>Slower execution: Real databases typically perform slower than in-memory databases like H2.</li>
 *   <li>Manual cleanup: Changes in the database structure may require manual cleanup, complicating test maintenance.</li>
 * </ul>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@DataJpaTest // Annotation for a JPA test that focuses only on JPA components
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
@Transactional(rollbackFor = Exception.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NoteRepositoryTest {
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private UserRepository userRepository;

    private Map<UserEntity, Set<NoteEntity>> userNotes;


    /**
     * <ol>
     *     <li>Deletes all from {@link UserRepository } & {@link NoteRepositoryTest}</li>
     *     <li>call to {@link #initializeAndPrepare()}</li>
     *     <li>Saves all the users and notes from {@code userNotes} map</li>
     * </ol>
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        noteRepository.deleteAll();

        // necessary with H2
        userRepository.flush();
        noteRepository.flush();

        // `userNotes`
        initializeAndPrepare();

        // save users then notes also
        userRepository.saveAllAndFlush(userNotes.keySet());
        noteRepository.saveAllAndFlush(userNotes.values().stream().flatMap(Set::stream).toList());// { {}, {} } -> { , , , , }
    }

    /**
     * Test {@link  NoteRepository#findByUser(UserEntity)} method.<br>
     * The method should return a list of notes(size=5) of each user.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     **/
    @Test
    void NoteRepository_findByUser_returnAListOfHisNotes() {
        // Arrange
        assertThat(noteRepository.count()).isEqualTo(10);
        assertThat(userRepository.count()).isEqualTo(2);

        for (UserEntity user : userNotes.keySet()) {
            // Act
            List<NoteEntity> notes = noteRepository.findByUser(user);

            // Assert
            assertThat(notes)
                    .hasSize(5)
                    .containsAll(userNotes.get(user));
        }
    }

    /**
     * Test {@link NoteRepository#findByIdAndUser(Long, UserEntity)}.<br>
     * Try to fetch each note(10) with each user(2), if the user is the owner of the note
     * then the method should return a non-empty {@code Optional} with the note, otherwise
     * it should return an empty {@code Optional}.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    void NoteRepository_findByIdAndUser_returnANonemptyOptionalIfIsHisNote() {
        // Arrange
        assertThat(userRepository.count()).isEqualTo(2);
        assertThat(noteRepository.count()).isEqualTo(10);

        for (UserEntity usr : userNotes.keySet()) {
            for (NoteEntity n : userNotes.values().stream().flatMap(Set::stream).toList()) { // { {}, {} } -> { , , , , }
                // Act
                Optional<NoteEntity> found = noteRepository.findByIdAndUser(n.getId(), usr);

                boolean isOwner = n.getUser().getId().equals(usr.getId());
                // Assert
                if (isOwner) {
                    assertThat(found).isPresent();
                    assertThat(found.get()).isEqualTo(n);

                } else assertThat(found).isEmpty();
            }
        }
    }

    /**
     * Test {@link  NoteRepository#findByUser(UserEntity, Pageable)}.<br>
     * The method should return the notes of the user in pages of 2 elements each.<br>
     * Here I get all the existent pages of notes of each user, the pages are sorted by
     * the {@link NoteEntity#getTitle()} field in ascending order.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    void NoteRepository_FindByUserPageable_returnPagesASC() {
        // Arrange
        assertThat(noteRepository.count()).isEqualTo(10);
        assertThat(userRepository.count()).isEqualTo(2);
        byte pageSize = 2;
        byte pageNumber = 0;
        Sort sort = Sort.by(Sort.Order.asc("title"));
        byte[] expectedEachPageElements = new byte[]{2, 2, 1};

        byte expectedTotalPages = 3;
        byte expectedTotalElements = 5;

        for (UserEntity usr : userNotes.keySet()) {
            pageNumber = 0;

            for (byte thisPageSize : expectedEachPageElements) {
                // Act
                Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
                Page<NoteEntity> page = noteRepository.findByUser(usr, pageable);

                // Assert
                assertThat(page).isNotNull();
                assertThat(page.getTotalPages()).isEqualTo(expectedTotalPages);
                assertThat(page.getTotalElements()).isEqualTo(expectedTotalElements);
                assertThat(page.getContent()).hasSize(thisPageSize);
                assertThat(page.getNumber()).isEqualTo(pageNumber);
                assertThat(page.getSize()).isEqualTo(pageSize);
                assertThat(page.stream().map(NoteEntity::getTitle))
                        .isSortedAccordingTo(Comparator.naturalOrder());

                if (++pageNumber >= page.getTotalPages()) break;
            }
        }
    }

    /**
     * Test {@link  NoteRepository#findByUser(UserEntity, Pageable)}<br>
     * The method should return the notes of the user in pages of 2 elements each.<br>
     * Here I get all the existent pages of notes of each user, the pages are sorted by
     * the {@code title} field in descending order.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    void NoteRepository_FindByUserPageable_returnPagesDES() {
        // Arrange
        assertThat(noteRepository.count()).isEqualTo(10);
        assertThat(userRepository.count()).isEqualTo(2);
        byte pageSize = 2;
        byte pageNumber = 0;
        Sort sort = Sort.by(Sort.Order.desc("title"));
        byte[] expectedEachPageElements = new byte[]{2, 2, 1};

        byte expectedTotalPages = 3;
        byte expectedTotalElements = 5;

        for (UserEntity usr : userNotes.keySet()) {
            pageNumber = 0;

            for (byte thisPageSize : expectedEachPageElements) {
                // Act
                Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
                Page<NoteEntity> page = noteRepository.findByUser(usr, pageable);

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


    /**
     * Initializes the {@code userNotes} map and prepares it for testing.<br>
     * Creates 2 users with 5 notes each one and assigns them to {@code userNotes} map.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void initializeAndPrepare() {
        userNotes = new HashMap<>();
        Set<NoteEntity> n1 = setUser(createNotes(1, 5), createUser("cris6h16"));
        Set<NoteEntity> n2 = setUser(createNotes(6, 10), createUser("github.com/cris6h16"));
        userNotes.put(n1.iterator().next().getUser(), n1);
        userNotes.put(n2.iterator().next().getUser(), n2);
    }


    /**
     * Creates a new {@link UserEntity}.
     *
     * @param username the username of the user
     * @return the created
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private UserEntity createUser(String username) {
        return UserEntity.builder()
                .username(username)
                .email(username + "@example.com")
                .password("12345678")
                .notes(new HashSet<>())
                .build();
    }

    /**
     * Creates a set of {@link NoteEntity} with the specified range of suffixes.
     *
     * @param startSuffix for the title and content
     * @param endSuffix   for the title and content
     * @return a set of notes
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private Set<NoteEntity> createNotes(int startSuffix, int endSuffix) {
        Set<NoteEntity> notes = new HashSet<>();
        for (int i = startSuffix; i <= endSuffix; i++) {
            notes.add(NoteEntity.builder().title("title" + i).content("content" + i).build());
        }
        return notes;
    }


    /**
     * Assigns the specified user to each note in the set.
     *
     * @param notes the set of notes
     * @param user  the user to assign
     * @return the set of notes with the user assigned
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private Set<NoteEntity> setUser(Set<NoteEntity> notes, UserEntity user) {
        for (NoteEntity note : notes) note.setUser(user);
        return notes;
    }
}