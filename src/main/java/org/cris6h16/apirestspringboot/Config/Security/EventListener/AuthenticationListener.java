package org.cris6h16.apirestspringboot.Config.Security.EventListener;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Utils.FilesUtils;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Vector;

/**
 * Listener for authentication events.
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Component
public class AuthenticationListener {
    private final FilesUtils filesUtils;
    protected volatile long lastSuccessFlushed;
    protected volatile long lastFailureFlushed;
    protected final List<SuccessData> successData;
    protected final List<FailureData> failureData;
    private final Object successLock = new Object();
    private final Object failureLock = new Object();
    private final long flushInterval = 10 * 60 * 1000; // 10 minutes

    public AuthenticationListener(FilesUtils filesUtils) {
        this.lastSuccessFlushed = 0L;
        this.lastFailureFlushed = 0L;
        this.filesUtils = filesUtils;
        this.successData = new Vector<>(10);
        this.failureData = new Vector<>(10);
    }

    /**
     * Listener for successful authentication events.
     *
     * @param success The event.
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        synchronized (successLock) {
            successData.add(new SuccessData(
                    success.getAuthentication(),
                    System.currentTimeMillis()
            ));
        }
        flushSuccessInFile();
    }

    /**
     * Listener for failed authentication events.
     *
     * @param failure The event.
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failure) {
        synchronized (failureLock) {
            failureData.add(new FailureData(
                    failure.getAuthentication(),
                    failure.getException(),
                    System.currentTimeMillis()
            ));
        }
        flushFailureInFile();
    }

    /**
     * Flushes the successful authentication events in the file.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    protected void flushSuccessInFile() {
        if (System.currentTimeMillis() - lastSuccessFlushed < flushInterval) return;

        synchronized (successLock) {
            if (System.currentTimeMillis() - lastSuccessFlushed < flushInterval) return;

            StringBuilder content = new StringBuilder();
            for (SuccessData data : successData) {
                content.append(data.toString()).append("\n");
            }

            this.filesUtils.appendToFile(
                    Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE),
                    content.toString()
            );

            this.successData.clear();
            lastSuccessFlushed = System.currentTimeMillis();
        }
    }

    /**
     * Flushes the failed authentication events in the file.
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void flushFailureInFile() {
        if (System.currentTimeMillis() - lastFailureFlushed < flushInterval) return;

        synchronized (failureLock) {
            if (System.currentTimeMillis() - lastFailureFlushed < flushInterval) return;

            StringBuilder content = new StringBuilder();
            for (FailureData data : failureData) {
                content.append(data.toString()).append("\n");
            }

            this.filesUtils.appendToFile(
                    Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE),
                    content.toString()
            );

            this.failureData.clear();
            lastFailureFlushed = System.currentTimeMillis();
        }
    }

    /**
     * Wrapper for successful authentication data. If you want to add more
     * data for be saved, you can do it here.
     *
     * @param authentication The authentication object.
     * @param instant        The instant when the event occurred.
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    protected record SuccessData(Authentication authentication, Long instant) {
    }

    /**
     * Wrapper for failed authentication data. If you want to add more
     * data for be saved, you can do it here.
     *
     * @param authentication The authentication object.
     * @param exception      The exception thrown.
     * @param instant        The instant when the event occurred.
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    protected record FailureData(Authentication authentication, Exception exception, Long instant) {
    }
}