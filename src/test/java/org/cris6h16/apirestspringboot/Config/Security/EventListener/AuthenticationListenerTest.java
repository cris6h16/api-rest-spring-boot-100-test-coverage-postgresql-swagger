package org.cris6h16.apirestspringboot.Config.Security.EventListener;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Utils.FilesSyncUtils;
import org.cris6h16.apirestspringboot.Utils.SychFor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class AuthenticationListenerTest {


    @Mock
    private FilesSyncUtils filesUtils;

    @InjectMocks
    private AuthenticationListener authenticationListener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticationListener.lastFlushed = 0L;
    }

    @Test
    void testOnSuccess() {
        // Arrange
        AuthenticationSuccessEvent successEvent = mock(AuthenticationSuccessEvent.class);
        Authentication authentication = mock(Authentication.class);
        when(successEvent.getAuthentication()).thenReturn(authentication);

        // Act
        authenticationListener.onSuccess(successEvent);

        // Assert
        // Verify that the `successData` list is updated and flushInFile is called
        assertFalse(authenticationListener.successData.isEmpty());
        verify(filesUtils, atLeastOnce()).appendToFile(
                eq(Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE)),
                anyString(),
                eq(SychFor.SUCCESS_DATA)
        );
    }

    @Test
    void testOnFailure() {
        // Arrange
        AbstractAuthenticationFailureEvent failureEvent = mock(AbstractAuthenticationFailureEvent.class);
        Authentication authentication = mock(Authentication.class);
        AuthenticationException exception = mock(AuthenticationException.class);
        when(failureEvent.getAuthentication()).thenReturn(authentication);
        when(failureEvent.getException()).thenReturn(exception);

        // Act
        authenticationListener.onFailure(failureEvent);

        // Assert
        // Verify that the `failureData` list is updated and flushInFile is called
        assertFalse(authenticationListener.failureData.isEmpty());
        verify(filesUtils, atLeastOnce()).appendToFile(
                eq(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)),
                anyString(),
                eq(SychFor.FAILURE_DATA)
        );
    }

    @Test
    void testFlushInFile_justCollected_successData() {
        // Arrange
        AuthenticationListener.SuccessData successData =
                new AuthenticationListener.SuccessData(
                        mock(Authentication.class),
                        System.currentTimeMillis()
                );


        authenticationListener.successData.add(successData);

        // Act
        authenticationListener.flushInFile();

        // Assert
        // Verify that `appendToFile` is just 1 time with the exact provided content
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE)),
                argThat(content -> content.equals(successData.toString())),
                eq(SychFor.SUCCESS_DATA)
        );
    }


    @Test
    void testFlushInFile_justCollected_failureData() {
        // Arrange
        AuthenticationListener.FailureData failureData =
                new AuthenticationListener.FailureData(
                        mock(Authentication.class),
                        mock(AuthenticationException.class),
                        System.currentTimeMillis()
                );

        authenticationListener.failureData.add(failureData);

        // Act
        authenticationListener.flushInFile();

        // Assert
        // Verify that `appendToFile` is just 1 time with the exact provided content
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)),
                argThat(content -> content.equals(failureData.toString())),
                eq(SychFor.FAILURE_DATA)
        );
    }

    @Test
    void testFlushInFile_bothCollected() {
        // Arrange
        AuthenticationListener.SuccessData successData =
                new AuthenticationListener.SuccessData(
                        mock(Authentication.class),
                        System.currentTimeMillis()
                );

        AuthenticationListener.FailureData failureData =
                new AuthenticationListener.FailureData(
                        mock(Authentication.class),
                        mock(AuthenticationException.class),
                        System.currentTimeMillis()
                );

        authenticationListener.successData.add(successData);
        authenticationListener.failureData.add(failureData);

        // Act
        authenticationListener.flushInFile();

        // Assert
        // Verify that `appendToFile` is called 2 times, one for successData and one for failureData
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE)),
                argThat(content -> content.equals(successData.toString())),
                eq(SychFor.SUCCESS_DATA)
        );
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)),
                argThat(content -> content.equals(failureData.toString())),
                eq(SychFor.FAILURE_DATA)
        );
    }
}