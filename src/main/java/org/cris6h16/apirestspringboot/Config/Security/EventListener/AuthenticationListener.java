package org.cris6h16.apirestspringboot.Config.Security.EventListener;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Utils.FilesUtils;
import org.cris6h16.apirestspringboot.Utils.SychFor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Vector;

/**
 * This class listens for authentication events and logs them in files
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Component
public class AuthenticationListener {
    private final FilesUtils filesUtils;
    protected volatile long lastFlushed;
    protected List<SuccessData> successData;
    protected List<FailureData> failureData;

    public AuthenticationListener(FilesUtils filesUtils) {
        this.lastFlushed = 0L;
        this.filesUtils = filesUtils;
        // using Vector for thraed safety
        this.successData = new Vector<>(10);
        this.failureData = new Vector<>(10);
    }

    /**
     * Logs the successful authentication
     *
     * @param success the event containing the authentication
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        successData.add(new SuccessData(
                success.getAuthentication(),
                System.currentTimeMillis()
        ));

        flushInFile();
    }

    /**
     * Logs the failed authentication
     *
     * @param failure the event containing the authentication and the exception
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failure) {
        failureData.add(new FailureData(
                failure.getAuthentication(),
                failure.getException(),
                System.currentTimeMillis()
        ));

        flushInFile();
    }

    /**
     * Flushes the data in the corresponding files
     * <p>
     * The data is flushed if the last flush was more than 10 minutes ago
     * finally the lists are cleared
     * </p>
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void flushInFile() {
        if (System.currentTimeMillis() - lastFlushed < (10 * 60 * 1000)) return; // if less than 10 minutes

        StringBuilder content;

        // Success
        if (!successData.isEmpty()) {
            content = new StringBuilder();
            for (SuccessData data : successData) content.append(data.toString()).append("\n");

            this.filesUtils.appendToFile(
                    Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE),
                    content.toString(),
                    SychFor.SUCCESS_DATA
            );
            this.successData.clear();
        }

        // Failure
        if (!failureData.isEmpty()) {
            content = new StringBuilder();
            for (FailureData data : failureData) content.append(data.toString()).append("\n");
            this.filesUtils.appendToFile(
                    Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE),
                    content.toString(),
                    SychFor.FAILURE_DATA
            );
            this.failureData.clear();
        }

        lastFlushed = System.currentTimeMillis();
    }


    /**
     * Container for the data extracted from the successful authentication
     * event, if we want log more data simply add it here
     *
     * @param authentication the authentication object
     * @param instant        the instant when the event was triggered
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    protected record SuccessData(Authentication authentication, Long instant) {
    }

    /**
     * Container for the data extracted from the failed authentication
     *
     * @param authentication the authentication object
     * @param exception      the exception thrown
     * @param instant        the instant when the event was triggered
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    protected record FailureData(Authentication authentication, Exception exception, Long instant) {
    }
}
