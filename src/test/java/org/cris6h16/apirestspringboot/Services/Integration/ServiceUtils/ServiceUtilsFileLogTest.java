package org.cris6h16.apirestspringboot.Services.Integration.ServiceUtils;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.Services.NoteServiceImpl;
import org.cris6h16.apirestspringboot.Services.UserServiceImpl;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the correct behavior when an unhandled exception is thrown in the service layer,
 * that exception will be logged in a file accumulating all the unhandled exceptions
 * in that file.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceUtilsFileLogTest {

    @Autowired
    private NoteServiceImpl noteService;
    @Autowired
    private UserServiceImpl userService;

    static Path p = Path.of(Cons.Logs.HiddenExceptionsOfUsers);

    /**
     * Delete the file before all the tests
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @BeforeAll
    static void beforeAll() throws IOException {
        if (Files.exists(p)) Files.delete(p);
    }

    /**
     * Unhandled exception in the {@link UserServiceImpl} layer, this
     * unhandled exception must be logged in the file, to achieve
     * this, the exception will act as exception thrown in production
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(1)
    void logUnhandledExceptionInFile_When_UserServiceThrowsIt() throws IOException {
        // Arrange
        assertThat(Files.notExists(p)).isTrue();

        CreateUserDTO dto = new CreateUserDTO() {
            @Override
            public String getEmail() {
                throw new LazyInitializationException("TEST EXCEPTION: IGNORE THIS = this exception will be handled as in prod");
            }
        };
        dto.setUsername("cris6h16");
        dto.setPassword("12345678");
        dto.setEmail("githubcomcris6h16@gmail.com");

        // Act
        try {
            userService.create(dto);
        } catch (Exception ignored) {
        }

        // Assert
        assertThat(Files.exists(p)).isTrue();
        assertThat(Files.readString(p))
                .contains("LazyInitializationException")
                .contains("TEST EXCEPTION: IGNORE THIS = this exception will be handled as in prod")
                .contains("org.cris6h16.apirestspringboot.Service.Integration.ServiceUtils.ServiceUtilsFileLogTest"); // stackTrace starting

    }

    /**
     * Unhandled exception in the {@link NoteServiceImpl} layer, this
     * unhandled exception must be logged in the file, to achieve
     * this, the exception will act as exception thrown in production
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Order(2)
    void logUnhandledExceptionInFile_When_NoteServiceThrowsIt() throws IOException {
        // Arrange
        assertThat(Files.exists(p)).isTrue();
        assertThat(Files.readAllLines(p)).hasSize(1);

        CreateNoteDTO dto = new CreateNoteDTO() {
            @Override
            public String getTitle() {
                throw new NullPointerException("TEST EXCEPTION: IGNORE THIS = this exception will be handled as in prod"); // this exception.msg doesn't contain the pattern that says: Not print StackTrace && Not log
            }
        };
        dto.setContent("content");
        dto.setTitle("title");

        // Act
        try {
            noteService.create(dto, userService.create(this.createUpdateUserDTO()));
        } catch (Exception ignored) {
        }

        // Assert
        assertThat(Files.exists(p)).isTrue();
        assertThat(Files.readString(p))
                .contains("NullPointerException")
                .contains("TEST EXCEPTION: IGNORE THIS = this exception will be handled as in prod")
                .contains("org.cris6h16.apirestspringboot.Service.Integration.ServiceUtils.ServiceUtilsFileLogTest"); // stackTrace starting

    }

    /**
     * Verify that the file contains the two exceptions thrown in the previous tests
     * and that the file is not empty
     *
     * @throws IOException if an I/O error occurs
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @see #logUnhandledExceptionInFile_When_UserServiceThrowsIt()
     * @see #logUnhandledExceptionInFile_When_NoteServiceThrowsIt()
     * @since 1.0
     */
    @Test
    @Order(3)
    void verifyTheAppend() throws IOException {
        assertThat(Files.exists(p)).isTrue();
        assertThat(Files.readAllLines(p)).hasSize(2);
        assertThat(Files.readAllLines(p).get(0)).contains("LazyInitializationException");
        assertThat(Files.readAllLines(p).get(1)).contains("NullPointerException");
        assertThat(Files.readAllLines(p).get(0)).contains("TEST EXCEPTION: IGNORE THIS = this exception will be handled as in prod");
        assertThat(Files.readAllLines(p).get(1)).contains("TEST EXCEPTION: IGNORE THIS = this exception will be handled as in prod");
    }

    /**
     * @return a {@link CreateUserDTO}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    CreateUserDTO createUpdateUserDTO() {
        return CreateUserDTO.builder()
                .username("hello-word" + "cris6h16")
                .password("hello-word" + "12345678")
                .email("hello-word" + "cristianmherrera21@gmail.com")
                .build();
    }
}

