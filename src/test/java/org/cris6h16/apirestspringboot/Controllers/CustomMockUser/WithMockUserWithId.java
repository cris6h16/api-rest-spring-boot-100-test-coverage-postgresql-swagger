package org.cris6h16.apirestspringboot.Controllers.CustomMockUser;

import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This annotation is used to mock a {@link UserWithId}.
 * this annotation uses the {@link WithSecurityContext} annotation to create a custom security context
 * which is created by the {@link WithMockCustomUserSecurityContextFactory} class.
 *
 * @author <a href="https://github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockUserWithId {
    long id() default 1L;

    String username() default "cris6h16";

    String password() default "12345678";

    String[] roles() default {"ROLE_USER"};
}

/**
 * This class is used to create a custom security context for the {@link WithMockUserWithId} annotation.
 *
 * @author <a href="www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockUserWithId> {

    /**
     * Create a {@link SecurityContext} given an  {@link WithMockUserWithId } Annotation.
     * @param customUser the {@link WithMockUserWithId} to create the {@link SecurityContext}
     * from. Cannot be null.
     * @return the {@link SecurityContext} to use. Cannot be null.
     *
     * @autor <a href="www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Override
    public SecurityContext createSecurityContext(WithMockUserWithId customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        String[] roles = customUser.roles();
        Set<? extends GrantedAuthority> authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        UserWithId principal = new UserWithId(
                customUser.id(),
                customUser.username(),
                customUser.password(),
                true,
                true,
                true,
                true,
                authorities);

        context.setAuthentication(new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities()));
        return context;
    }
}
//
//class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {
//    public CustomAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
//        super(principal, credentials, authorities);
//    }
//}
