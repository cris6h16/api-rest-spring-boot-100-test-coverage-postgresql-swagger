package org.cris6h16.apirestspringboot.Controllers.UserController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Services.UserServiceImpl;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.cris6h16.apirestspringboot.Constants.Cons.User.Controller.Path.USER_PATH;


/**
 * Controller of the only admin endpoints to work with {@link UserServiceImpl}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@RestController
@RequestMapping(USER_PATH)
public class AdminUserController {

    UserServiceImpl userService;

    public AdminUserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Operation(
            tags = {"Admin User Endpoints"},
            operationId = "getUsersPage",
            summary = "get users page",
            description = "Get a page of users",
            method = "GET",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Page of users",
                            content = @Content(
                                    schema = @Schema(implementation = Page.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = {
                                            @ExampleObject(
                                                    name = "Page of users",
                                                    value = """
                                                            {
                                                                "content": [
                                                                    {
                                                                        "id": 1,
                                                                        "username": "cris6h16",
                                                                        "email": "cristianmherrera21@gmail.com",
                                                                        "createdAt": "2024-07-22",
                                                                        "updatedAt": null,
                                                                        "roles": [
                                                                            {
                                                                                "name": "ROLE_ADMIN"
                                                                            }
                                                                        ],
                                                                        "notes": []
                                                                    },
                                                                    {
                                                                        "id": 3,
                                                                        "username": "cris6h16-1",
                                                                        "email": "cristianmherrera21-1@gmail.com",
                                                                        "createdAt": "2024-07-22",
                                                                        "updatedAt": null,
                                                                        "roles": [
                                                                            {
                                                                                "name": "ROLE_USER"
                                                                            }
                                                                        ],
                                                                        "notes": []
                                                                    },
                                                                    {
                                                                        "id": 4,
                                                                        "username": "cris6h16-2",
                                                                        "email": "cristianmherrera21-2@gmail.com",
                                                                        "createdAt": "2024-07-22",
                                                                        "updatedAt": null,
                                                                        "roles": [
                                                                            {
                                                                                "name": "ROLE_USER"
                                                                            }
                                                                        ],
                                                                        "notes": []
                                                                    }
                                                                ],
                                                                "pageable": {
                                                                    "pageNumber": 0,
                                                                    "pageSize": 10,
                                                                    "sort": {
                                                                        "sorted": true,
                                                                        "unsorted": false,
                                                                        "empty": false
                                                                    },
                                                                    "offset": 0,
                                                                    "paged": true,
                                                                    "unpaged": false
                                                                },
                                                                "totalPages": 1,
                                                                "totalElements": 3,
                                                                "last": true,
                                                                "first": true,
                                                                "size": 10,
                                                                "number": 0,
                                                                "sort": {
                                                                    "sorted": true,
                                                                    "unsorted": false,
                                                                    "empty": false
                                                                },
                                                                "numberOfElements": 3,
                                                                "empty": false
                                                            }
                                                            """,
                                                    summary = "Page of users",
                                                    description = "Page of users, the content is a list of PublicUserDTO"
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Any unexpected error occurred while processing the request ( is not ADMIN, database error, etc. )",
                            content = @Content
                    )
            },
            security = {
                    @SecurityRequirement(name = "basicAuth")
            }
    )

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<PublicUserDTO>> getPage(
            @PageableDefault(
                    size = Cons.User.Page.DEFAULT_SIZE,
                    page = Cons.User.Page.DEFAULT_PAGE,
                    sort = Cons.User.Page.DEFAULT_SORT,
                    direction = Sort.Direction.ASC
            ) @ParameterObject Pageable pageable) {
        Page<PublicUserDTO> p = userService.getPage(pageable);
        return ResponseEntity.ok(p);
    }
}