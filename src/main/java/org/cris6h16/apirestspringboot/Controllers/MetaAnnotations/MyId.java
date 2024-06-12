package org.cris6h16.apirestspringboot.Controllers.MetaAnnotations;


import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.User;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.security.Principal;

/**
 * Get the {@code id} of the {@code principal}, this annotation must be used after of
 * {@code @PreAuthorize("isAuthenticated()")} or any which check if the user is authenticated.
 * <br><br>
 * Explanation:
 * {@code authentication.name.equalsIgnoreCase('anonymousUser') ? -1}
 * <p>
 * is necessary because even if you put {@code @PreAuthorize("isAuthenticated()")} before of all,
 * you will get an SpEL exception, spring first evaluates the syntax of SpEL.
 * <p>
 * I have a custom impl of {@link User} which has a
 * {@code getId()} method({@link UserWithId}). if I'm not logged in.
 * then the {@link java.security.Principal} hasn't an attribute {@code id}(by default).
 * but if I'm logged in,then the {@link Principal} has an
 * attribute {@code id}(I'm using a custom impl when I'm logged in).
 * So, due to the above {@code authentication.name.equalsIgnoreCase('anonymousUser')} will tell to me
 * if I'm logged in ({@link Principal} has an {@code id}) or not ({@link Principal}
 * hasn't an {@code id})....
 * <br><br>
 * then if I use {@code @PreAuthorize("isAuthenticated()")} , Spring first evaluates the SpEL, later
 * see if is authenticated or not. it means that the {@code -1} is never reached(remember use
 * {@code @PreAuthorize("isAuthenticated()")} or any which can avoid reach to {@link MyId} if You aren;t aunthenticated).
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@CurrentSecurityContext(expression = "authentication.name.equalsIgnoreCase('anonymousUser') ? -1 : authentication.principal.id")
public @interface MyId {
}
