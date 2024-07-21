package org.cris6h16.apirestspringboot.Controllers.UserController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.ExceptionHandler.ErrorResponse;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchEmailUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchPasswordUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchUsernameUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Services.UserServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller of the authenticated endpoints to work with {@link UserServiceImpl}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@RestController
@RequestMapping(Cons.User.Controller.Path.USER_PATH)
public class AuthenticatedUserController {
    UserServiceImpl userService;

    public AuthenticatedUserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Operation(
            tags = {"Authenticated User Endpoints"},
            operationId = "getById",
            summary = "get user by id",
            description = "Get a user by its id",
            method = "GET",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User found, then returned",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PublicUserDTO.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Any unexpected error occurred while processing the request ( user not found, trying retrieve other user's data, database error, etc. )",
                            content = @Content
                    )
            },
            security = {
                    @SecurityRequirement(name = "basicAuth")
            }
    )
    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PublicUserDTO> getById(@PathVariable(required = true) Long id) {
        PublicUserDTO u = userService.getById(id);
        return ResponseEntity.ok(u);
    }


    @Operation(
            tags = {"Authenticated User Endpoints"},
            operationId = "patchUsernameById",
            summary = "patch username by id",
            description = "Patch the username of a user by its id",
            method = "PATCH",
            security = {
                    @SecurityRequirement(name = "basicAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The new username to patch",
                    required = true,
                    content = @Content(
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PatchUsernameUserDTO.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Username patched successfully",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Any unexpected error occurred while processing the request ( user not found, trying patch other user's data, database error, etc. )",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Email already exists",
                            content = @Content(
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    )
            }
    )
    @PatchMapping(
            value = Cons.User.Controller.Path.COMPLEMENT_PATCH_USERNAME + "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> patchUsernameById(@PathVariable(required = true) Long id,
                                                  @RequestBody(required = true)  PatchUsernameUserDTO dto) {
        userService.patchUsernameById(id, dto);
        return ResponseEntity.noContent().build();
    }

//    @Operation(
//            tags = {"Authenticated User Endpoints"},
//            operationId = "patchEmailById",
//            summary = "patch email by id",
//            description = "Patch the email of a user by its id",
//            method = "PATCH",
//            security = {
//                    @SecurityRequirement(name = "basicAuth")
//            },
//            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    description = "The new email to patch",
//                    required = true,
//                    content = @Content(
//                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PatchEmailUserDTO.class),
//                            mediaType = MediaType.APPLICATION_JSON_VALUE
//                    )
//            ),
//            responses = {
//                    @ApiResponse(
//                            responseCode = "204",
//                            description = "Email patched successfully",
//                            content = @Content
//                    ),
//                    @ApiResponse(
//                            responseCode = "409",
//                            description = "Already exists an user with the same email",
//                            content = @Content(
//                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class),
//                                    mediaType = MediaType.APPLICATION_JSON_VALUE
//                            )
//                    ),
//
//            }
//    )
    @PatchMapping(
            value = Cons.User.Controller.Path.COMPLEMENT_PATCH_EMAIL + "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> patchEmailById(@PathVariable(required = true) Long id,
                                               @RequestBody(required = true)  PatchEmailUserDTO dto) {
        userService.patchEmailById(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(
            value = Cons.User.Controller.Path.COMPLEMENT_PATCH_PASSWORD + "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> patchPasswordById(@PathVariable(required = true) Long id,
                                                  @RequestBody(required = true)  PatchPasswordUserDTO dto) {
        userService.patchPasswordById(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable(required = true) Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
