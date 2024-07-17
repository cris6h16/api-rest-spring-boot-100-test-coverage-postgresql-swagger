package org.cris6h16.apirestspringboot.Config.Security.CustomUser;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserWithId}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
class UserWithIdTest {


    @Test
    void testGetId() {
        UserWithId user = mock(UserWithId.class);
        when(user.getId()).thenReturn(1L);

        assertEquals(1L, user.getId());
    }

    @Test
    void testInstanceOfUserDetails() {
        UserWithId user = mock(UserWithId.class);
        assertTrue(user instanceof UserDetails); // UserWithId >> User >> UserDetails
    }
}