package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.MetaAnnotations.MyId;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.controller.UserControllerTransversalException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserServiceTransversalException;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
     * @return {@link ResponseEntity} with the location of the new user
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
     * @param principalId injected, of the principal that is asking for the data
     * @return {@link ResponseEntity} with the user data
     * @throws UserControllerTransversalException if {@code {id} != principal.id}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @GetMapping(value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<PublicUserDTO> get(@PathVariable Long id,
                                             @MyId Long principalId) {
        verifyOwnership(id, principalId);
        PublicUserDTO u = userService.get(id);
        return ResponseEntity.ok(u);
    }

    /**
     * Update a user<br>
     * make it through: {@link UserService#update(Long, CreateUpdateUserDTO)}
     *
     * @param id          of the user to update
     * @param dto         with just the fields that we want to update not blank
     * @param principalId injected, of the principal that is trying to update the user
     * @return {@link ResponseEntity} with no content
     * @throws UserServiceTransversalException if {@code {id} != principal.id}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @PatchMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @RequestBody CreateUpdateUserDTO dto,
                                       @MyId Long principalId) {//TODO: Impl boudary cases for all @CONTROLLERS
        verifyOwnership(id, principalId);
        userService.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a user<br>
     * make it through: {@link UserService#delete(Long)}
     *
     * @param id          of the user to delete
     * @param principalId injected, of the principal that is trying to delete the user
     * @return {@link ResponseEntity} with no content
     * @throws UserControllerTransversalException if {@code {id} != principal.id}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @MyId Long principalId) {
        verifyOwnership(id, principalId);
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PublicUserDTO>> getUsers(Pageable pageable) {
        List<PublicUserDTO> l = userService.get(pageable);
        return ResponseEntity.ok(l);
    }

    private void verifyOwnership(Long id, Long principalId) {
        if (!id.equals(principalId)) {
            throw new UserControllerTransversalException(Cons.Auth.Fails.IS_NOT_YOUR_ID_MSG, HttpStatus.FORBIDDEN);
        }
    }
}
