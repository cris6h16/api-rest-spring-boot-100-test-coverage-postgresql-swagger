package org.cris6h16.apirestspringboot.Controllers.UserController;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Services.Interfaces.UserService;
import org.cris6h16.apirestspringboot.Services.UserServiceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(Cons.User.Controller.Path.USER_PATH)
public class AdminUserController {

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