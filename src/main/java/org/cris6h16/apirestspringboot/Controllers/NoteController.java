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


    /*
     /*
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
     */
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
                            responseCode = "403",
                            description = "Any unexpected error occurred while processing the request ( user isn't authenticated, database error, etc. )",
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
//
//
//    @Operation(
//            tags = {"Note Endpoints"},
//            operationId = "getNotesPage",
//            summary = "get notes",
//            description = "Get a page of notes",
//            method = "GET",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "Page of notes found, then returned",
//                            content = @Content(
//                                    array = @ArraySchema(schema = @Schema(implementation = PublicNoteDTO.class)),
//                                    mediaType = MediaType.APPLICATION_JSON_VALUE
//                            ),
//
//                    ),
//                    @ApiResponse(
//                            responseCode = "403",
//                            description = "Any unexpected error occurred while processing the request ( user isn't authenticated, database error, etc. )",
//                            content = @Content
//                    )
//            },
//            security = {
//                    @SecurityRequirement(name = "basicAuth")
//            }
//    )
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Page<PublicNoteDTO>> getPage(
            @PageableDefault(
                    size = Cons.Note.Page.DEFAULT_SIZE,
                    page = Cons.Note.Page.DEFAULT_PAGE,
                    sort = Cons.Note.Page.DEFAULT_SORT,
                    direction = Sort.Direction.ASC
            ) Pageable pageable,
            @MyId Long principalId) {
        Page<PublicNoteDTO> list = noteService.getPage(pageable, principalId);
        return ResponseEntity.ok(list);
    }

    /**
     * Get a {@link NoteEntity} by id<br>
     * Uses: {@link NoteServiceImpl#getByIdAndUserId(Long, Long)}
     *
     * @param noteId      of the note to getById
     * @param principalId injected, the id of the principal; the user that is getting the note
     * @return {@link ResponseEntity} with the note data
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @GetMapping(
            value = "/{noteId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PublicNoteDTO> getByIdAndUserId(@PathVariable(required = true) Long noteId,
                                                          @MyId Long principalId) {
        PublicNoteDTO en = noteService.getByIdAndUserId(noteId, principalId);
        return ResponseEntity.ok(en);
    }

    /**
     * Update a {@link NoteEntity}<br>
     * Uses: {@link NoteServiceImpl#putByIdAndUserId(Long, Long, CreateNoteDTO)}
     *
     * @param noteId      of the note to update
     * @param note        to be PUT
     * @param principalId injected, the id of the principal; the user that is updating the note
     * @return {@link ResponseEntity#noContent()} if successful
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @PutMapping(
            value = "/{noteId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> putByIdAndUserId(@PathVariable(required = true) Long noteId,
                                                 @MyId Long principalId,
                                                 @RequestBody(required = true) CreateNoteDTO note) {
        noteService.putByIdAndUserId(noteId, principalId, note);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a {@link NoteEntity}<br>
     * Uses: {@link NoteServiceImpl#deleteByIdAndUserId(Long, Long)}
     *
     * @param noteId      of the note to deleteById
     * @param principalId injected, the id of the principal; the user that is deleting the note
     * @return {@link ResponseEntity#noContent()} if successful
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @DeleteMapping(value = "/{noteId}")
    public ResponseEntity<Void> deleteByIdAndUserId(@PathVariable(required = true) Long noteId,
                                                    @MyId Long principalId) {
        noteService.deleteByIdAndUserId(noteId, principalId);
        return ResponseEntity.noContent().build();
    }
}
