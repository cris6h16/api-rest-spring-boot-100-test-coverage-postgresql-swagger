package org.cris6h16.apirestspringboot.Repositories;

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
     *     <li>Saves all the users and notes from {@link #userNotes} map</li>
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
     * Test {@link NoteRepository#findByIdAndUserId(Long, Long)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    void findByIdAndUserId_returnANonemptyOptionalIfIsHisNote() {
        // Arrange
        assertThat(userRepository.count()).isEqualTo(2);
        assertThat(noteRepository.count()).isEqualTo(10);

        for (UserEntity usr : userNotes.keySet()) {
            for (NoteEntity n : userNotes.values().stream().flatMap(Set::stream).toList()) { // { {}, {} } -> { , , , , }
                // Act
                Optional<NoteEntity> noteFromDB = noteRepository.findByIdAndUserId(n.getId(), usr.getId());

                boolean isOwner = n.getUser().getId().equals(usr.getId());

                // Assert
                if (isOwner) {
                    assertThat(noteFromDB).isPresent();
                    assertThat(noteFromDB.get()).isEqualTo(n);
                } else assertThat(noteFromDB).isEmpty();
            }
        }
    }


    /**
     * Test {@link NoteRepository#existsByIdAndUserId(Long, Long)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    void existsByIdAndUserId_trueIfIsHisNote() {
        // Arrange
        assertThat(userRepository.count()).isEqualTo(2);
        assertThat(noteRepository.count()).isEqualTo(10);

        for (UserEntity usr : userNotes.keySet()) {
            for (NoteEntity n : userNotes.values().stream().flatMap(Set::stream).toList()) { // { {}, {} } -> { , , , , }
                // Act
                boolean exists = noteRepository.existsByIdAndUserId(n.getId(), usr.getId());

                boolean isOwner = n.getUser().getId().equals(usr.getId());

                // Assert
                assertThat(exists).isEqualTo(isOwner);
            }
        }
    }

    /**
     * Test {@link NoteRepository#deleteByIdAndUserId(Long, Long)}
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    void deleteByIdAndUserId_deleteIfIsHisNote() {
        // Arrange
        assertThat(userRepository.count()).isEqualTo(2);
        assertThat(noteRepository.count()).isEqualTo(10);

        for (UserEntity usr : userNotes.keySet()) {
            for (NoteEntity n : userNotes.values().stream().flatMap(Set::stream).toList()) { // { {}, {} } -> { , , , , }
                // Act
                noteRepository.deleteByIdAndUserId(n.getId(), usr.getId());

                // Assert
                boolean existsBefore = noteRepository.existsById(n.getId()); // In the second iteration the first 5 notes will be non-existent.
                boolean shouldHaveBeenDeleted = n.getUser().getId().equals(usr.getId());
                boolean existsAfter = noteRepository.existsById(n.getId());

                if (existsBefore && shouldHaveBeenDeleted) {
                    assertThat(existsAfter).isFalse();
                } else {
                    assertThat(existsAfter).isEqualTo(existsBefore);
                }

            }
            // verify that all notes of the user were deleted
            assertThat(noteRepository.findAll().stream()
                    .anyMatch(
                            note -> note.getUser().getId().equals(usr.getId())
                    )
            ).isFalse();
        }
        // should be empty
        assertThat(noteRepository.count()).isEqualTo(0);
    }


    /**
     * Test {@link NoteRepository#findByUserId(Long, Pageable)} sorted by {@code title}
     * in ascending order.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    void findByUserId_pageable_returnPagesASC() {
        // Arrange
        assertThat(noteRepository.count()).isEqualTo(10);
        assertThat(userRepository.count()).isEqualTo(2);
        byte pageSize = 2;
        byte pageNumber;
        Sort sort = Sort.by(Sort.Order.asc("title"));
        byte[] expectedEachPageElements = new byte[]{2, 2, 1};

        byte expectedTotalPages = 3;
        byte expectedTotalElements = 5;

        for (UserEntity usr : userNotes.keySet()) {
            pageNumber = 0;

            for (byte thisPageSize : expectedEachPageElements) {
                // Act
                Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
                Page<NoteEntity> page = noteRepository.findByUserId(usr.getId(), pageable);

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
     * Test {@link NoteRepository#findByUserId(Long, Pageable)} sorted by {@code title}
     * in descending order.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    void FindByUserPageable_returnPagesDES() {
        // Arrange
        assertThat(noteRepository.count()).isEqualTo(10);
        assertThat(userRepository.count()).isEqualTo(2);
        byte pageSize = 2;
        byte pageNumber;
        Sort sort = Sort.by(Sort.Order.desc("title"));
        byte[] expectedEachPageElements = new byte[]{2, 2, 1};

        byte expectedTotalPages = 3;
        byte expectedTotalElements = 5;

        for (UserEntity usr : userNotes.keySet()) {
            pageNumber = 0;

            for (byte thisPageSize : expectedEachPageElements) {
                // Act
                Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
                Page<NoteEntity> page = noteRepository.findByUserId(usr.getId(), pageable);

                // Assert
                assertThat(page).isNotNull();
                assertThat(page.getTotalPages()).isEqualTo(expectedTotalPages);
                assertThat(page.getTotalElements()).isEqualTo(expectedTotalElements);
                assertThat(page.getContent()).hasSize(thisPageSize);
                assertThat(page.getNumber()).isEqualTo(pageNumber);
                assertThat(page.getSize()).isEqualTo(pageSize);
                assertThat(page.stream().map(NoteEntity::getTitle))
                        .isSortedAccordingTo(Comparator.reverseOrder());

                if (++pageNumber >= page.getTotalPages()) break;
            }
        }
    }


    /**
     * Initializes the {@link #userNotes} map and prepares it for testing.<br>
     * Creates 2 users with 5 notes each one and assigns them to {@link #userNotes} map.
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
                .createdAt(new Date())
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
            notes.add(NoteEntity.builder().title("title" + i).content("content" + i).updatedAt(new Date()).build());
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