package org.cris6h16.apirestspringboot.Config.Security;

import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testing for {@link WebSecurity#checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(Supplier, String)}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
class WebSecurityTest {

    @InjectMocks
    WebSecurity webSecurity;

    @Test
    @Order(1)
    void testGranted() {
        Long pathVariable_UserId = 1L;

        UserWithId user = mock(UserWithId.class);
        when(user.getId()).thenReturn(1L);
        when(user.getAuthorities())
                .thenReturn(List.of(new SimpleGrantedAuthority(ERole.ROLE_USER.toString())));

        UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
        when(token.getPrincipal()).thenReturn(user);
        Supplier<Authentication> supplier = () -> token;

        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId.toString());

        assertTrue(granted);
    }

    @Test
    @Order(2)
    void testNotGranted() {
        String pathVariable_UserId = "2";

        UserWithId user = mock(UserWithId.class);
        when(user.getId()).thenReturn(1L);
        when(user.getAuthorities())
                .thenReturn(List.of(new SimpleGrantedAuthority(ERole.ROLE_USER.toString())));

        UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
        when(token.getPrincipal()).thenReturn(user);
        Supplier<Authentication> supplier = () -> token;

        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId);

        assertFalse(granted);
    }


    @Test
    void hasRoleAdmin_SamePathVariableAndPrincipalId_thenTrue() {
        String pathVariable_UserId = "2";

        UserWithId user = mock(UserWithId.class);
        when(user.getId()).thenReturn(2L);
        when(user.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority(ERole.ROLE_ADMIN.toString())));

        UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
        when(token.getPrincipal()).thenReturn(user);
        Supplier<Authentication> supplier = () -> token;

        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId);

        assertTrue(granted);
    }

    @Test
    void hasRoleAdmin_DifferentPathVariableAndPrincipalId_thenFalse() {
        String pathVariable_UserId = "2";

        UserWithId user = mock(UserWithId.class);
        when(user.getId()).thenReturn(1L);
        when(user.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority(ERole.ROLE_ADMIN.toString())));

        UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
        when(token.getPrincipal()).thenReturn(user);
        Supplier<Authentication> supplier = () -> token;

        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId);

        assertFalse(granted);
    }

    @Test
    void hasRoleAdmin_ButPrincipalIdIsNull_thenFalse() {
        String pathVariable_UserId = "2";

        UserWithId user = mock(UserWithId.class);
        when(user.getId()).thenReturn(null);
        when(user.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority(ERole.ROLE_ADMIN.toString())));

        UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
        when(token.getPrincipal()).thenReturn(user);
        Supplier<Authentication> supplier = () -> token;

        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId);

        assertFalse(granted);
    }


    @Test
    void hasRoleUser_SamePathVariableAndPrincipalId_thenTrue() {
        String pathVariable_UserId = "2";

        UserWithId user = mock(UserWithId.class);
        when(user.getId()).thenReturn(2L);
        when(user.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority(ERole.ROLE_USER.toString())));

        UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
        when(token.getPrincipal()).thenReturn(user);
        Supplier<Authentication> supplier = () -> token;

        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId);

        assertTrue(granted);
    }

    @Test
    void hasRoleUser_DifferentPathVariableAndPrincipalId_thenFalse() {
        String pathVariable_UserId = "2";

        UserWithId user = mock(UserWithId.class);
        when(user.getId()).thenReturn(1L);
        when(user.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority(ERole.ROLE_USER.toString())));

        UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
        when(token.getPrincipal()).thenReturn(user);
        Supplier<Authentication> supplier = () -> token;

        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId);

        assertFalse(granted);
    }

    @Test
    void hasRoleUser_ButPrincipalIdIsNull_thenFalse() {
        String pathVariable_UserId = "2";

        UserWithId user = mock(UserWithId.class);
        when(user.getId()).thenReturn(null);
        when(user.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority(ERole.ROLE_USER.toString())));

        UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
        when(token.getPrincipal()).thenReturn(user);
        Supplier<Authentication> supplier = () -> token;

        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId);

        assertFalse(granted);
    }

    @Test
    void hasNoRoleUserOrAdmin_thenFalse() {
        String pathVariable_UserId = "2";

        UserWithId user = mock(UserWithId.class);
        when(user.getAuthorities())
                .thenReturn(List.of(new SimpleGrantedAuthority("ROLE_HELLOWORD")));

        UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
        when(token.getPrincipal()).thenReturn(user);
        Supplier<Authentication> supplier = () -> token;

        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId);

        assertFalse(granted);
    }

    @Test
    void hasRolesEmpty_thenFalse() {
        String pathVariable_UserId = "2";

        UserWithId user = mock(UserWithId.class);
        when(user.getAuthorities())
                .thenReturn(List.of());

        UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
        when(token.getPrincipal()).thenReturn(user);
        Supplier<Authentication> supplier = () -> token;

        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId);

        assertFalse(granted);
    }


    @Test
    void hasRolesNull_thenFalse() {
        String pathVariable_UserId = "2";

        UserWithId user = mock(UserWithId.class);
        when(user.getAuthorities())
                .thenReturn(null);

        UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
        when(token.getPrincipal()).thenReturn(user);
        Supplier<Authentication> supplier = () -> token;

        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId);

        assertFalse(granted);
    }

    @Test
    void SupplierNull_thenFalse() {
        String pathVariable_UserId = "2";
        Supplier<Authentication> supplier = null;
        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId);

        assertFalse(granted);
    }

    @Test
    void IdNull_thenFalse() {
        String pathVariable_UserId = null;
        UserWithId user = mock(UserWithId.class);
        when(user.getId()).thenReturn(1L);
        when(user.getAuthorities())
                .thenReturn(List.of(new SimpleGrantedAuthority(ERole.ROLE_USER.toString())));

        UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
        when(token.getPrincipal()).thenReturn(user);
        Supplier<Authentication> supplier = () -> token;

        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId);

        assertFalse(granted);
    }

    @Test
    void InvalidId_thenFalse() {
        String pathVariable_UserId = "users";
        UserWithId user = mock(UserWithId.class);
        when(user.getId()).thenReturn(1L);
        when(user.getAuthorities())
                .thenReturn(List.of(new SimpleGrantedAuthority(ERole.ROLE_USER.toString())));

        UsernamePasswordAuthenticationToken token = mock(UsernamePasswordAuthenticationToken.class);
        when(token.getPrincipal()).thenReturn(user);
        Supplier<Authentication> supplier = () -> token;

        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId);

        assertFalse(granted);
    }

    @Test
    void AuthenticationNull_thenFalse() {
        String pathVariable_UserId = "2";
        Supplier<Authentication> supplier = () -> null;
        boolean granted = webSecurity.checkIfIsAdminOrUserAndHasThisIdAsPrincipalId(supplier, pathVariable_UserId);

        assertFalse(granted);
    }
}