package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Rest Controller for {@link UserService}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@RestController
@RequestMapping(UserController.path)
public class UserController {
    public static final String path = "/api/users";

    UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    /**
     * Create a new user<br>
     * make it through: {@link UserService#create(CreateUpdateUserDTO)}
     *
     * @param user {@link CreateUpdateUserDTO} with the data of the new user
     * @return {@link ResponseEntity#created(URI)} with the location of the new user
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE // if is successful else the defined on Advice
    )
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> create(@RequestBody CreateUpdateUserDTO user) {
        Long id = userService.create(user);
        URI uri = URI.create(path + "/" + id);
        return ResponseEntity.created(uri).build();
    }

    /**
     * Get a user by id<br>
     * make it through: {@link UserService#get(Long)}
     *
     * @param id          of the user to get
     * @return {@link ResponseEntity} with the user data
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @GetMapping(value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated() and hasAnyRole('ADMIN', 'USER') and #id == authentication.principal.id")
    public ResponseEntity<PublicUserDTO> get(@PathVariable Long id) {
        PublicUserDTO u = userService.get(id);
        return ResponseEntity.ok(u);
    }

    /**
     * Update a user<br>
     * make it through: {@link UserService#update(Long, CreateUpdateUserDTO)}
     *
     * @param id          of the user to update
     * @param dto         with just the fields that we want to update not blank
     * @return {@link ResponseEntity} with no content
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @PatchMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated() and hasAnyRole('ADMIN', 'USER') and #id == authentication.principal.id")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @RequestBody CreateUpdateUserDTO dto) {//TODO: Impl boudary cases for all @CONTROLLERS
        userService.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a user<br>
     * make it through: {@link UserService#delete(Long)}
     *
     * @param id          of the user to delete
     * @return {@link ResponseEntity} with no content
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated() and hasAnyRole('ADMIN', 'USER') and #id == authentication.principal.id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all users<br>
     * make it through: {@link UserService#get(Pageable)}
     *
     * @param pageable the page request
     * @return {@link ResponseEntity} with a list of users
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    public ResponseEntity<List<PublicUserDTO>> getUsers(Pageable pageable) {
        List<PublicUserDTO> l = userService.get(pageable);
        return ResponseEntity.ok(l);
    }
}
