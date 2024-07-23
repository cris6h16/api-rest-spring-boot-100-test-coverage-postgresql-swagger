package org.cris6h16.apirestspringboot.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Controllers.ExceptionHandler.ErrorResponse;
import org.cris6h16.apirestspringboot.Controllers.MetaAnnotations.MyId;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicNoteDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Services.NoteServiceImpl;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Controller for {@link NoteServiceImpl}}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@RestController
@RequestMapping(path = NoteController.path)
public class NoteController {
    public static final String path = Cons.Note.Controller.Path.NOTE_PATH;
    private final NoteServiceImpl noteService;

    public NoteController(NoteServiceImpl noteService) {
        this.noteService = noteService;
    }


    @Operation(
            tags = {"Note Endpoints"},
            operationId = "createANote",
            summary = "create note",
            description = "Create a note",
            method = "POST",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Note created, then returned Location header with the new note's location",
                            headers = {
                                    @io.swagger.v3.oas.annotations.headers.Header(
                                            name = "Location",
                                            description = "The location of the new note",
                                            schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string"),
                                            example = Cons.Note.Controller.Path.NOTE_PATH + "/1"
                                    )
                            },
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Any bad request ( only title too long at the moment. )",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Title too long",
                                            summary = "Title too long",
                                            description = "The title is too long, the maximum length is " + Cons.Note.Validations.MAX_TITLE_LENGTH,
                                            value = """
                                                    {
                                                        "message": "Title must be less than 255 characters",
                                                        "status": "400 BAD_REQUEST",
                                                        "instant": "2024-07-21T22:32:54.466134778Z"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Any unexpected error occurred while processing the request ( request body is not a JSON, database error, etc. )",
                            content = @Content
                    )
            },
            security = {
                    @SecurityRequirement(name = "basicAuth")
            }
    )
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> create(@RequestBody(required = true) CreateNoteDTO note,
                                       @MyId @Parameter(hidden = true) Long principalId) {
        Long id = noteService.create(note, principalId);
        URI uri = URI.create(path + "/" + id);

        return ResponseEntity.created(uri).build();
    }


    @Operation(
            tags = {"Note Endpoints"},
            operationId = "getNotesPage",
            summary = "get notes page",
            description = "Get a page of notes",
            method = "GET",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Page of notes found, then returned",
                            content = @Content(
                                    schema = @Schema(implementation = Page.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = {
                                            @ExampleObject(
                                                    name = "Page of notes",
                                                    value = """
                                                            {
                                                                "content": [
                                                                    {
                                                                        "id": 1,
                                                                        "title": "Mi primera nota",
                                                                        "content": "Contenido de mi primera nota",
                                                                        "updatedAt": "2024-07-22"
                                                                    },
                                                                    {
                                                                        "id": 2,
                                                                        "title": "Mi primera segunda",
                                                                        "content": "Contenido de mi segunda nota",
                                                                        "updatedAt": "2024-07-22"
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
                                                                "totalElements": 2,
                                                                "last": true,
                                                                "first": true,
                                                                "size": 10,
                                                                "number": 0,
                                                                "sort": {
                                                                    "sorted": true,
                                                                    "unsorted": false,
                                                                    "empty": false
                                                                },
                                                                "numberOfElements": 2,
                                                                "empty": false
                                                            }
                                                            """,
                                                    summary = "Page of notes",
                                                    description = "Page of notes found, then returned"
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Any unexpected error occurred while processing the request ( tried sort by a non-existent field, database error, etc. )",
                            content = @Content
                    )
            },
            security = {
                    @SecurityRequirement(name = "basicAuth")
            }
    )
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Page<PublicNoteDTO>> getPage(
            @PageableDefault(
                    size = Cons.Note.Page.DEFAULT_SIZE,
                    page = Cons.Note.Page.DEFAULT_PAGE,
                    sort = Cons.Note.Page.DEFAULT_SORT,
                    direction = Sort.Direction.ASC
            ) @ParameterObject  Pageable pageable,
            @MyId @Parameter(hidden = true) Long principalId) {
        Page<PublicNoteDTO> list = noteService.getPage(pageable, principalId);
        return ResponseEntity.ok(list);
    }


    @Operation(
            tags = {"Note Endpoints"},
            operationId = "getNoteById",
            summary = "get note",
            description = "Get a note by its id",
            method = "GET",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Note found, then returned",
                            content = @Content(
                                    schema = @Schema(implementation = PublicNoteDTO.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = {
                                            @ExampleObject(
                                                    name = "Note",
                                                    value = """
                                                            {
                                                                 "id": 1,
                                                                 "title": "Mi primera segunda",
                                                                 "content": "Contenido de mi segunda nota",
                                                                 "updatedAt": "2024-07-22"
                                                             }
                                                            """,
                                                    summary = "Note found",
                                                    description = "Note found, then returned"
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Note not found",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Note not found",
                                            summary = "Note not found",
                                            description = "The note with the id 10 was not found",
                                            value = """
                                                    {
                                                        "message": "Note not found",
                                                        "status": "404 NOT_FOUND",
                                                        "instant": "2024-07-22T22:58:51.351548210Z"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Any unexpected error occurred while processing the request ( /{noteId} passed is not a number, database error, etc. )",
                            content = @Content
                    )
            },
            security = {
                    @SecurityRequirement(name = "basicAuth")
            }
    )
    @GetMapping(
            value = "/{noteId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PublicNoteDTO> getByIdAndUserId(@PathVariable(required = true) Long noteId,
                                                          @MyId @Parameter(hidden = true) Long principalId) {
        PublicNoteDTO en = noteService.getByIdAndUserId(noteId, principalId);
        return ResponseEntity.ok(en);
    }


    @Operation(
            tags = {"Note Endpoints"},
            operationId = "putNoteById",
            summary = "Put note",
            description = "Update a note by its id",
            method = "PUT",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Note was Put",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Any bad request ( only title too long at the moment. )",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Title too long",
                                            summary = "Title too long",
                                            description = "The title is too long, the maximum length is " + Cons.Note.Validations.MAX_TITLE_LENGTH,
                                            value = """
                                                    {
                                                        "message": "Title must be less than 255 characters",
                                                        "status": "400 BAD_REQUEST",
                                                        "instant": "2024-07-21T22:32:54.466134778Z"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Any unexpected error occurred while processing the request ( /{noteId} passed is not a number, database error, etc. )",
                            content = @Content
                    )
            },
            security = {
                    @SecurityRequirement(name = "basicAuth")
            }
    )
    @PutMapping(
            value = "/{noteId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> putByIdAndUserId(@PathVariable(required = true) Long noteId,
                                                 @MyId @Parameter(hidden = true) Long principalId,
                                                 @RequestBody(required = true) CreateNoteDTO note) {
        noteService.putByIdAndUserId(noteId, principalId, note);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            tags = {"Note Endpoints"},
            operationId = "deleteNoteById",
            summary = "delete note",
            description = "Delete a note by its id",
            method = "DELETE",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Note deleted",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Note not found",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = "Note not found",
                                            summary = "Note not found",
                                            description = "The note with the id 10 was not found",
                                            value = """
                                                    {
                                                        "message": "Note not found",
                                                        "status": "404 NOT_FOUND",
                                                        "instant": "2024-07-22T23:17:00.480561850Z"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User not authenticated",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Any unexpected error occurred while processing the request ( /{noteId} passed is not a number, database error, etc. )",
                            content = @Content
                    )
            },
            security = {
                    @SecurityRequirement(name = "basicAuth")
            }
    )
    @DeleteMapping(value = "/{noteId}")
    public ResponseEntity<Void> deleteByIdAndUserId(@PathVariable(required = true) Long noteId,
                                                    @MyId @Parameter(hidden = true) Long principalId) {
        noteService.deleteByIdAndUserId(noteId, principalId);
        return ResponseEntity.noContent().build();
    }
}
