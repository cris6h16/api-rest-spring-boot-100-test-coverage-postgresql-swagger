
package org.cris6h16.apirestspringboot.Config.Service.CustomAuthHandler;
import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Service(value = "authResponses")
public class MyAuthorizationService {
    //    @PreAuthorize("#id == authentication.principal.id")
    public boolean checkIfIsOwnerOfThisId(Long id) {
        if (id < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id must be greater than 0");

        Principal principal = SecurityContextHolder.getContext().getAuthentication();
        if (((UsernamePasswordAuthenticationToken) principal).getPrincipal() instanceof UserWithId) {
            UserWithId user = (UserWithId) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            if (!user.getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You aren't the owner of this id");
            }
        }

        return true;
    }

    public boolean checkIfIsAuthenticated() {
        Principal principal = SecurityContextHolder.getContext().getAuthentication();
        if (((Authentication) principal).getPrincipal().equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to be authenticated to perform this action");
        }

        return true;
    }

    public boolean returnTrue(){
        return true;
    }


}
