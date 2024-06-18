package org.cris6h16.apirestspringboot.Controllers.UserController;

import jakarta.validation.constraints.NotNull;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(AdminUserController.path)
@PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
public class AdminUserController {
    public static final String path = Cons.User.Controller.PATH;

    UserServiceImpl userService;

    public AdminUserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    /**
     * Get all users<br>
     * make it using: {@link UserService#getPage(Pageable)}
     *
     * @param pageable the page request
     * @return {@link ResponseEntity} with a list of users
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PublicUserDTO>> getPage(
            @PageableDefault(
                    size = Cons.User.Page.DEFAULT_SIZE,
                    page = Cons.User.Page.DEFAULT_PAGE,
                    sort = Cons.User.Page.DEFAULT_SORT,
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {
        List<PublicUserDTO> l = userService.getPage(pageable);
        return ResponseEntity.ok(l);
    }
}