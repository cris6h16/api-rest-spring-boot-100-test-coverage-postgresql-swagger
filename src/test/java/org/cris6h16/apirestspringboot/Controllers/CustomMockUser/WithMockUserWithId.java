package org.cris6h16.apirestspringboot.Controllers.CustomMockUser;

import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * // todo: doc this && the above classes && about a custom security context
 * @author <a href="https://github.com/cris6h16" target="_blank">Cristian Herrera</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockUserWithId {
    long id() default 1L;

    String username() default "cris6h16";

    String password() default "12345678";

    String[] roles() default {"ROLE_USER"};
}


class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockUserWithId> {
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
