package org.cris6h16.apirestspringboot.Config.Security.CustomUser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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