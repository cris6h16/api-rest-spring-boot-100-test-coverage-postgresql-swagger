package org.cris6h16.apirestspringboot.Controllers.ExceptionHandler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.CustomMockUser.WithMockUserWithId;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.ProperExceptionForTheUser;
import org.cris6h16.apirestspringboot.Utils.FilesUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlerControllersTest {

    @InjectMocks
    private ExceptionHandlerControllers exceptionHandlerControllers;

    @Mock
    private FilesUtils filesSyncUtils;

    @Test
    void handleConstraintViolationException_withViolations_Then400_BAD_REQUEST_andCustomMsg() throws Exception {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("My custom message in the validation");
        ConstraintViolationException exception = new ConstraintViolationException("Constraint violation", Set.of(violation));

        ResponseEntity<String> res = this.exceptionHandlerControllers.handleConstraintViolationException(exception);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).contains("\"message\":\"My custom message in the validation\"");
    }

    @Test
    void handleConstraintViolationException_withoutViolations_Then400_BAD_REQUEST_andGenericMsg() throws Exception {
        ConstraintViolationException exception = new ConstraintViolationException("Constraint violation", new HashSet<>());

        ResponseEntity<String> res = this.exceptionHandlerControllers.handleConstraintViolationException(exception);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).contains("\"message\":\"?m\"".replace("?m", Cons.Response.ForClient.GENERIC_ERROR));
    }

    @Test
    void handleDataIntegrityViolationException_withUsernameUniqueConstraint_Then409_CONFLICT_andCustomMsg() throws Exception {
        DataIntegrityViolationException e = mock(DataIntegrityViolationException.class);
        when(e.getMessage()).thenReturn("any string here" + Cons.User.Constrains.USERNAME_UNIQUE_NAME + "also here");

        ResponseEntity<String> res = this.exceptionHandlerControllers.handleDataIntegrityViolationException(e);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res.getBody()).contains("\"message\":\"?m\"".replace("?m", Cons.User.Constrains.USERNAME_UNIQUE_MSG));
    }

    @Test
    void handleDataIntegrityViolationException_withEmailUniqueConstraint_Then409_CONFLICT_andCustomMsg() throws Exception {
        DataIntegrityViolationException e = mock(DataIntegrityViolationException.class);
        when(e.getMessage()).thenReturn("any string here" + Cons.User.Constrains.EMAIL_UNIQUE_NAME + "also here");

        ResponseEntity<String> res = this.exceptionHandlerControllers.handleDataIntegrityViolationException(e);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res.getBody()).contains("\"message\":\"?m\"".replace("?m", Cons.User.Constrains.EMAIL_UNIQUE_MSG));
    }

    @Test
    void handleDataIntegrityViolationException_withUnhandledUniqueConstraint_Then409_CONFLICT_andGenericMsg() throws Exception {
        DataIntegrityViolationException e = mock(DataIntegrityViolationException.class);
        when(e.getMessage()).thenReturn("any string here" + "dni_unique" + "also here");

        ResponseEntity<String> res = this.exceptionHandlerControllers.handleDataIntegrityViolationException(e);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res.getBody()).contains("\"message\":\"?m\"".replace("?m", Cons.Response.ForClient.GENERIC_ERROR));
    }

    @Test
    void handleMethodArgumentNotValidException_WithViolations_Then400_BAD_REQUEST_andValidationMsg() throws Exception {
        ObjectError error = mock(ObjectError.class);
        when(error.getDefaultMessage()).thenReturn("My custom message in the validation");
        MethodArgumentNotValidException e = mock(MethodArgumentNotValidException.class);
        when(e.getAllErrors()).thenReturn(List.of(error));

        ResponseEntity<String> res = this.exceptionHandlerControllers.handleMethodArgumentNotValidException(e);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).contains("\"message\":\"My custom message in the validation\"");
    }


    // for reach 100% coverage
    @Test
    void handleMethodArgumentNotValidException_WithNoViolations_Then400_BAD_REQUEST_andGenericMsg() throws Exception {
        MethodArgumentNotValidException e = mock(MethodArgumentNotValidException.class);
        when(e.getAllErrors()).thenReturn(List.of());

        ResponseEntity<String> res = this.exceptionHandlerControllers.handleMethodArgumentNotValidException(e);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody()).contains("\"message\":\"?m\"".replace("?m", Cons.Response.ForClient.GENERIC_ERROR));
    }

    @Test
    void handleProperExceptionForTheUser_ThenStatusAndMsgFromTheException() throws Exception {
        ProperExceptionForTheUser e = mock(ProperExceptionForTheUser.class);
        when(e.getStatus()).thenReturn(HttpStatus.VARIANT_ALSO_NEGOTIATES);
        when(e.getReason()).thenReturn("My custom message in the exception 123");

        ResponseEntity<String> res = this.exceptionHandlerControllers.handleProperExceptionForTheUser(e);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.VARIANT_ALSO_NEGOTIATES);
        assertThat(res.getBody()).contains("\"message\":\"My custom message in the exception 123\"");
    }

    @Test
    void handleException_Then403() throws Exception {
        NullPointerException e = mock(NullPointerException.class);
        when(e.toString()).thenReturn("NullPointerException: Unexpected exception");
        when(e.getStackTrace()).thenReturn(new StackTraceElement[0]);

        ResponseEntity<String> res = this.exceptionHandlerControllers.handleException(e);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(res.getBody()).isNull();
        verify(this.filesSyncUtils, times(1))
                .appendToFile(
                        eq(Path.of(Cons.Logs.HiddenExceptionsOfUsers)),
                        argThat(line ->
                                line.contains(e.toString()) &&
                                        line.split("::").length == 3 &&
                                        line.charAt(line.length() - 1) == '\n'
                        )
                );
    }

    @Test
//    @WithMockUserWithId(roles = {"ROLE_ADMIN"}) // doesn't work, I'll make it manually
    void handleException_AsAdmin_ThenExceptionToStringInBody_AndExceptionNotSavedInFile() throws Exception {
        {
            UserWithId userWithId = mock(UserWithId.class);
            when(userWithId.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority(ERole.ROLE_ADMIN.toString())));

            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(userWithId);

            SecurityContext mockedContext = mock(SecurityContext.class);
            when(mockedContext.getAuthentication()).thenReturn(auth);

            SecurityContextHolder.setContext(mockedContext);
        }

        NullPointerException e = mock(NullPointerException.class);
        when(e.toString()).thenReturn("NullPointerException: Unexpected exception 123");

        ResponseEntity<String> res = this.exceptionHandlerControllers.handleException(e);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(res.getHeaders().get("Content-Type")).contains("application/json");
        assertThat(res.getBody()).contains(e.toString());
        verify(this.filesSyncUtils, never()).appendToFile(any(), any());

        SecurityContextHolder.clearContext();
    }


    @Test
    void handleException_Then403_ExceptionWithMessageNull_successfullySavedInFile() throws Exception {
        NullPointerException e = mock(NullPointerException.class);
        when(e.getStackTrace()).thenReturn(new StackTraceElement[0]);

        ResponseEntity<String> res = this.exceptionHandlerControllers.handleException(e);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(res.getBody()).isNull();
        verify(this.filesSyncUtils, times(1))
                .appendToFile(
                        eq(Path.of(Cons.Logs.HiddenExceptionsOfUsers)),
                        argThat(line ->
                                line.contains(e.toString()) &&
                                        line.split("::").length == 3 &&
                                        line.charAt(line.length() - 1) == '\n'
                        )
                );
    }

    @Test
    void handleException_Then403_ExceptionWithStackTraceNull_successfullySavedInFile() throws Exception {
        NullPointerException e = mock(NullPointerException.class);

        ResponseEntity<String> res = this.exceptionHandlerControllers.handleException(e);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(res.getBody()).isNull();
        verify(this.filesSyncUtils, times(1))
                .appendToFile(
                        eq(Path.of(Cons.Logs.HiddenExceptionsOfUsers)),
                        argThat(line ->
                                line.contains(e.toString()) &&
                                        line.split("::").length == 3 &&
                                        line.charAt(line.length() - 1) == '\n'
                        )
                );
    }

    @Test
    void handleException_Then403_ExceptionWithStackTraceLengthZero_successfullySavedInFile() throws Exception {
        NullPointerException e = mock(NullPointerException.class);
        when(e.getStackTrace()).thenReturn(new StackTraceElement[0]);

        ResponseEntity<String> res = this.exceptionHandlerControllers.handleException(e);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(res.getBody()).isNull();
        verify(this.filesSyncUtils, times(1))
                .appendToFile(
                        eq(Path.of(Cons.Logs.HiddenExceptionsOfUsers)),
                        argThat(line ->
                                line.contains(e.toString()) &&
                                        line.split("::").length == 3 &&
                                        line.charAt(line.length() - 1) == '\n'
                        )
                );
    }

    @Test
    void handleException_Then403_andShouldSaveExceptionInFile_ConcurrencyTest() throws InterruptedException {
        ExceptionHandlerControllers.lastSavedToFile = 0;
        ExceptionHandlerControllers.hiddenExceptionsLines.clear();
        ExceptionHandlerControllers.hiddenExceptionsLines.addAll(
                List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                        "21", "22", "23", "24", "25", "26", "27", "28", "29", "30")
        );

        ExecutorService executor = Executors.newFixedThreadPool(50);

        for (int i = 0; i < 50; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 30; j++) {
                    NullPointerException e = mock(NullPointerException.class);
                    when(e.toString()).thenReturn("NullPointerException: Unexpected exception");
                    this.exceptionHandlerControllers.handleException(e);
                }
            });
        }

        // Initiates an orderly shutdown in which previously submitted tasks are executed... see the java doc
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) { // wait for the executor to finish
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    Assertions.fail("Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        int totalThatWasEverListed = 50 * 30 + 30;
        int couldBeSaved = 1 + 30;
        int remaining = totalThatWasEverListed - couldBeSaved;

        assertThat(ExceptionHandlerControllers.hiddenExceptionsLines.size()).isEqualTo(remaining);
        verify(this.filesSyncUtils, times(1)).appendToFile(
                any(),
                argThat(content -> content.split("\n").length == couldBeSaved)
        );

        // clear invocations on the mock
        clearInvocations(this.filesSyncUtils);

        // save the remaining when 10 minutes passed since the last save ( simulated ) ( activate it calling the method )
        ExceptionHandlerControllers.lastSavedToFile = ExceptionHandlerControllers.lastSavedToFile - (ExceptionHandlerControllers.MILLIS_EACH_SAVE + 1);

        NullPointerException e = mock(NullPointerException.class);
        when(e.toString()).thenReturn("NullPointerException: Unexpected exception");
        this.exceptionHandlerControllers.handleException(e);
        remaining = remaining + 1;

        assertThat(ExceptionHandlerControllers.hiddenExceptionsLines.size()).isEqualTo(0);
        final int finalRemaining = remaining;
        verify(this.filesSyncUtils, times(1)).appendToFile(
                eq(Path.of(Cons.Logs.HiddenExceptionsOfUsers)),
                argThat(content -> content.split("\n").length == finalRemaining)
        );
    }
}
