package org.cris6h16.apirestspringboot.Config.Security.EventListener;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Utils.FilesSyncUtils;
import org.cris6h16.apirestspringboot.Utils.SychFor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link AuthenticationListener}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
class AuthenticationListenerTest {


    @Mock
    private FilesSyncUtils filesUtils;

    @InjectMocks
    private AuthenticationListener authenticationListener;


    /**
     * Test for {@link AuthenticationListener#onSuccess(AuthenticationSuccessEvent)}
     *
     * <p>
     * This test verifies that the list {@link AuthenticationListener#successData}
     * is updated and the method {@link AuthenticationListener#flushInFile()} is called
     * with the correct parameters.
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
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
        assertTrue(authenticationListener.successData.isEmpty());
        verify(filesUtils, atLeastOnce()).appendToFile(
                eq(Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE)),
                anyString(),
                eq(SychFor.SUCCESS_DATA)
        );
    }

    /**
     * Test for {@link AuthenticationListener#onFailure(AbstractAuthenticationFailureEvent)}
     *
     * <p>
     * This test verifies that the list {@link AuthenticationListener#failureData}
     * is updated and the method {@link AuthenticationListener#flushInFile()} is called
     * with the correct parameters.
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
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
        assertTrue(authenticationListener.failureData.isEmpty());
        verify(filesUtils, atLeastOnce()).appendToFile(
                eq(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)),
                anyString(),
                eq(SychFor.FAILURE_DATA)
        );
    }


    /**
     * Test for {@link AuthenticationListener#flushInFile()}
     *
     * <p>
     * This test verifies that the method {@link AuthenticationListener#flushInFile()}
     * works correctly when the list {@link AuthenticationListener#failureData} is empty
     * and the list {@link AuthenticationListener#successData} has just 1 element.
     * then the method {@link FilesSyncUtils#appendToFile(Path, String, SychFor)}
     * is called just 1 time with the correct parameters.
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("flushInFile")
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
        assertTrue(authenticationListener.successData.isEmpty());
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE)),
                argThat(content -> content.equals(successData.toString() + "\n")), // make sure that tha las char is a new line
                eq(SychFor.SUCCESS_DATA)
        );
        // Verify that `appendToFile` is never called for failureData
        verify(filesUtils, never()).appendToFile(
                eq(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)),
                anyString(),
                eq(SychFor.FAILURE_DATA)
        );
    }


    /**
     * Test for {@link AuthenticationListener#flushInFile()}
     *
     * <p>
     * This test verifies that the method {@link AuthenticationListener#flushInFile()}
     * works correctly when the list {@link AuthenticationListener#successData} is empty
     * and the list {@link AuthenticationListener#failureData} has just 1 element.
     * then the method {@link FilesSyncUtils#appendToFile(Path, String, SychFor)}
     * is called just 1 time with the correct parameters.
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("flushInFile")
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
        assertTrue(authenticationListener.failureData.isEmpty());
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)),
                argThat(content -> content.equals(failureData.toString() + "\n")), // make sure that tha las char is a new line
                eq(SychFor.FAILURE_DATA)
        );
    }

    /**
     * Test for {@link AuthenticationListener#flushInFile()}
     *
     * <p>
     * This test verifies that the method {@link AuthenticationListener#flushInFile()}
     * works correctly when the list {@link AuthenticationListener#failureData} &&
     * {@link AuthenticationListener#successData} has each one just 1 element.
     * then the method {@link FilesSyncUtils#appendToFile(Path, String, SychFor)}
     * is called twice. One for successData and one for failureData
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("flushInFile")
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
        assertTrue(authenticationListener.successData.isEmpty());
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE)),
                argThat(content -> content.equals(successData.toString() + "\n")),
                eq(SychFor.SUCCESS_DATA)
        );

        assertTrue(authenticationListener.failureData.isEmpty());
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)),
                argThat(content -> content.equals(failureData.toString() + "\n")),
                eq(SychFor.FAILURE_DATA)
        );
    }

    /**
     * Test for {@link AuthenticationListener#flushInFile()}
     *
     * <p>
     * This test verifies that the method {@link AuthenticationListener#flushInFile()}
     * works correctly when the list {@link AuthenticationListener#failureData} &&
     * {@link AuthenticationListener#successData} has 10 elements
     * each one then the method {@link FilesSyncUtils#appendToFile(Path, String, SychFor)}
     * is called twice. One for successData and one for failureData
     * <br>
     * Also verify the lines of the content passed to the file by both, it should be 10 each one
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    @Tag("both")
    void testFlushInFile_bothCollected_List10Elements() {
        // Arrange
        for (int i = 0; i < 10; i++) {
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
        }

        // Act
        authenticationListener.flushInFile();

        // Assert
        // Verify that `appendToFile` is called 2 times, one for successData and one for failureData
        assertTrue(authenticationListener.successData.isEmpty());
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE)),
                argThat(content -> content.split("\n").length == 10),
                eq(SychFor.SUCCESS_DATA)
        );

        assertTrue(authenticationListener.failureData.isEmpty());
        verify(filesUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE)),
                argThat(content -> content.split("\n").length == 10),
                eq(SychFor.FAILURE_DATA)
        );
    }
}