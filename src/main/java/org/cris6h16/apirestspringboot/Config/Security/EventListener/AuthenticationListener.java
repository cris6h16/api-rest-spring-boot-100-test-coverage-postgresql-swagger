package org.cris6h16.apirestspringboot.Config.Security.EventListener;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Utils.FilesSyncUtils;
import org.cris6h16.apirestspringboot.Utils.SychFor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Vector;

@Component
public class AuthenticationListener {
    private FilesSyncUtils filesUtils;
    private static final Logger log = LoggerFactory.getLogger(AuthenticationListener.class);
    protected volatile long lastFlushed; //todo: add logs
    protected volatile List<SuccessData> successData; // todo: volatile is needed? even if it's a Vector?
    protected volatile List<FailureData> failureData;

    public AuthenticationListener(FilesSyncUtils filesUtils) {
        this.lastFlushed = 0L;
        this.filesUtils = filesUtils;
        // using Vector for thraed safety
        this.successData = new Vector<>(10);
        this.failureData = new Vector<>(10);
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        List<String> roles = success
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        successData.add(new SuccessData(
                success.getAuthentication(),
                System.currentTimeMillis()
        ));

        flushInFile();
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failure) {
        AuthenticationException exception = failure.getException();

        failureData.add(new FailureData(
                failure.getAuthentication(),
                exception,
                System.currentTimeMillis()
        ));

        flushInFile();
    }


    void flushInFile() {
        if (System.currentTimeMillis() - lastFlushed < (10 * 60 * 1000)) return; // if less than 10 minutes

        StringBuilder content;

        // Success
        if (!successData.isEmpty()) {
            content = new StringBuilder();
            for (SuccessData data : successData) content.append(data.toString()).append("\n");
            content.deleteCharAt(content.length() - 1); //// remove the last line break

            this.filesUtils.appendToFile(
                    Path.of(Cons.Logs.SUCCESS_AUTHENTICATION_FILE),
                    content.toString(),
                    SychFor.SUCCESS_DATA
            );
        }

        // Failure
        if (!failureData.isEmpty()) {
            content = new StringBuilder();
            for (FailureData data : failureData) content.append(data.toString()).append("\n");
            content.deleteCharAt(content.length() - 1); //// remove the last line break
            this.filesUtils.appendToFile(
                    Path.of(Cons.Logs.FAIL_AUTHENTICATION_FILE),
                    content.toString(),
                    SychFor.FAILURE_DATA
            );
        }

        lastFlushed = System.currentTimeMillis();
    }


    protected record SuccessData(Authentication authentication, Long instant) {
    }

    protected record FailureData(Authentication authentication, Exception exception, Long instant) {
    }
}
