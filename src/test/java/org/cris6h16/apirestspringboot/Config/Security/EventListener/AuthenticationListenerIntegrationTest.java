package org.cris6h16.apirestspringboot.Config.Security.EventListener;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Utils.FilesUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
@Tag("IntegrationTest")
@ActiveProfiles(profiles = "test")
public class AuthenticationListenerIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private AuthenticationListener authenticationListener;

    @MockBean
    FilesUtils filesUtils;

    private String successFile;
    private String failureFile;

    public AuthenticationListenerIntegrationTest() {
        this.successFile = Cons.Logs.SUCCESS_AUTHENTICATION_FILE;
        this.failureFile = Cons.Logs.FAIL_AUTHENTICATION_FILE;
    }

    @BeforeEach
    void setUp() {
        Mockito.clearInvocations(filesUtils);
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
        verify(filesUtils, times(1)).appendToFile(
                argThat(s -> s.toString().equals(successFile)),
                argThat(s -> s.contains("SuccessData[authentication=") &&
                        s.contains("instant=") &&
                        s.endsWith("\n")
                )
        );
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
        verify(filesUtils, times(1)).appendToFile(
                argThat(s -> s.toString().equals(failureFile)),
                argThat(s -> s.contains("FailureData[authentication=") &&
                        s.contains("instant=") &&
                        s.endsWith("\n")
                )
        );
    }
}