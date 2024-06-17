package org.cris6h16.apirestspringboot.Controllers.UserController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchEmailUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchPasswordUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchUsernameUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;
import org.cris6h16.apirestspringboot.Service.UserServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AuthenticatedUserController.path)
@PreAuthorize("isAuthenticated() and hasAnyRole('ADMIN', 'USER')")
public class AuthenticatedUserController {
    public static final String path = Cons.User.Controller.PATH;

    UserServiceImpl userService;

    public AuthenticatedUserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    /**
     * Get a user by id<br>
     * make it through: {@link UserService#get(Long)}
     *
     * @param id of the user to get
     * @return {@link ResponseEntity} with the user data
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<PublicUserDTO> get(@PathVariable(required = true) Long id) {
        PublicUserDTO u = userService.get(id);
        return ResponseEntity.ok(u);
    }


    @PatchMapping(value = "/patch/username/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<Void> patchUsernameById(@PathVariable(required = true) Long id,
                                                  @RequestBody(required = true) @Valid PatchUsernameUserDTO dto) {
        userService.patchUsernameById(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/patch/email/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<Void> patchEmailById(@PathVariable(required = true) Long id,
                                               @RequestBody(required = true) @Valid PatchEmailUserDTO dto) {
        userService.patchEmailById(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/patch/password/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<Void> patchPassword(@PathVariable(required = true) Long id,
                                              @RequestBody(required = true) @Valid PatchPasswordUserDTO dto) {
        userService.patchPasswordById(id, dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a user<br>
     * make it through: {@link UserService#delete(Long)}
     *
     * @param id of the user to delete
     * @return {@link ResponseEntity} with no content
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
