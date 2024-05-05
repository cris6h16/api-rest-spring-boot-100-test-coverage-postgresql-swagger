
package org.cris6h16.apirestspringboot.Config.Service.CustomAuthHandler;

import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * This class is a service that returns custom responses for authorization | authentication failures.<br>
 * remember that this is only a part of the "custom responses" the rest is in the {@link org.cris6h16.apirestspringboot.Controllers.ExceptionHandler.ExceptionHandlerControllers} class
 *
 * @author github.com/cris6h16
 */
@Service(value = "AuthCustomResponses")
public class MyAuthorizationService {
    //    @PreAuthorize("#id == authentication.principal.id")
    public boolean checkIfIsOwnerOfThisId(Long id) {
        if (id < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id must be greater than 0");

        // if something goes wrong, we won't authorize. simply throw an exception
        Principal principal = SecurityContextHolder.getContext().getAuthentication();
        if (((UsernamePasswordAuthenticationToken) principal).getPrincipal() instanceof UserWithId) {
            UserWithId user = (UserWithId) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            if (user.getId().equals(id)) return true;
            else throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You aren't the owner of this id");
        }

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong when we tried to return a specific response of authentication failure");
    }

    // if isn't a USER role, we won't authorize. you can let more roles or separate => `checkIfIsUser()`
    public boolean checkIfIsAuthenticated() {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities();

        boolean isUser = authorities.stream().anyMatch(r -> r.getAuthority().equalsIgnoreCase(ERole.ROLE_USER.toString()));
        boolean isNotAuthenticated = authorities.stream().anyMatch(r -> r.getAuthority().equalsIgnoreCase(ERole.ROLE_ANONYMOUS.toString()));
        if (isUser) return true;
        else if (isNotAuthenticated)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be authenticated to perform this action");


        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong when we tried to return a specific response of authentication failure, probably you added a new Role");
    }
}
