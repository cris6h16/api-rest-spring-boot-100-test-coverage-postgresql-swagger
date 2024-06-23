package org.cris6h16.apirestspringboot.Config.Security.EventListener;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)/* we won't use it, but it's necessary for load the context with H2 as DB, then run the tests isolated from the real database*/
@Tag("IntegrationTest")
public class AuthenticationListenerIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private AuthenticationListener authenticationListener;

    @BeforeEach
    void setUp() throws IOException {
        deleteLogFilesIfExist();
        authenticationListener.lastSuccessFlushed = 0L; // avoid wait for 10 mins to flush
        authenticationListener.lastFailureFlushed = 0L; // avoid wait for 10 mins to flush
    }

    @Test
    void testSuccessEventPublished() throws IOException {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        AuthenticationSuccessEvent successEvent = new AuthenticationSuccessEvent(authentication);

        // Act
        eventPublisher.publishEvent(successEvent);

        // Assert
        // Verify that the successData list is updated
        assertTrue(authenticationListener.successData.isEmpty());
        assertThatFailureFileNotExists();
        assertThatSuccessFileExistsAndContainALineContainingKeywords();
    }

    private void assertThatFailureFileNotExists() {
        assertFalse(Files.exists(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)));
    }

    private void assertThatSuccessFileExistsAndContainALineContainingKeywords() throws IOException {
        Path path = Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE);
        assertTrue(Files.exists(path));
        String allLines = Files.readString(path);

        assertThat(allLines.split("\n").length).isEqualTo(1);
        assertThat(allLines)
                .contains("SuccessData[authentication=")
                .contains("instant=")
                .endsWith("\n");
    }

    private void deleteLogFilesIfExist() throws IOException {
        Files.deleteIfExists(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE));
        Files.deleteIfExists(Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE));
    }

    @Test
    void testFailureEventPublished() throws IOException {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        AuthenticationException exception = mock(AuthenticationException.class);
        AbstractAuthenticationFailureEvent failureEvent = mock(AbstractAuthenticationFailureEvent.class);
        when(failureEvent.getAuthentication()).thenReturn(authentication);
        when(failureEvent.getException()).thenReturn(exception);

        // Act
        eventPublisher.publishEvent(failureEvent);

        // Assert
        // Verify that the failureData list is updated
        assertTrue(authenticationListener.failureData.isEmpty());
        assertThatSuccessFileNotExists();
        assertThatFailureFileExistsAndContainALineContainingKeywords();
    }

    private void assertThatSuccessFileNotExists() {
        Path p = Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE);
        assertFalse(Files.exists(p));
    }

    private void assertThatFailureFileExistsAndContainALineContainingKeywords() throws IOException {
        Path path = Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE);
        assertTrue(Files.exists(path));
        String allLines = Files.readString(path);

        assertThat(allLines.split("\n").length).isEqualTo(1);
        assertThat(allLines)
                .contains("FailureData[authentication=")
                .contains("instant=")
                .endsWith("\n");
    }
}