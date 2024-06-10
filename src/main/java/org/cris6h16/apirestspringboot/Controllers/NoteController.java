package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Controllers.MetaAnnotations.MyId;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Service.NoteServiceImpl;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controller for {@link NoteServiceImpl}}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@RestController //@Controller && @ResponseBody
@RequestMapping(path = NoteController.path)
@PreAuthorize("isAuthenticated()")
public class NoteController {

    public static final String path = "/api/notes";
    private final NoteServiceImpl noteService;

    public NoteController(NoteServiceImpl noteService) {
        this.noteService = noteService;
    }


    /**
     * Create a {@link NoteEntity}
     *
     * @param note        {@link CreateNoteDTO}
     * @param principalId {@link Long}
     * @return {@link ResponseEntity#created(URI)} with the location of the created note
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE // if is successful else the defined on Advice
    )
    public ResponseEntity<Void> create(@RequestBody CreateNoteDTO note, @MyId Long principalId) {
        Long id = noteService.create(note, principalId);
        URI uri = URI.create(path + "/" + id);

        return ResponseEntity.created(uri).build();
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<PublicNoteDTO>> getPage(Pageable pageable, @MyId Long principalId) {
        List<PublicNoteDTO> list = noteService.getPage(pageable, principalId);
        return ResponseEntity.ok(list);
    }

    @GetMapping(
            value = "/{noteId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PublicNoteDTO> get(@PathVariable Long noteId, @MyId Long principalId) {
        PublicNoteDTO en = noteService.get(noteId, principalId);
        return ResponseEntity.ok(en);
    }

    @PutMapping(
            value = "/{noteId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> update(@PathVariable Long noteId,
                                       @RequestBody CreateNoteDTO note,
                                       @MyId Long principalId) {
        noteService.put(noteId, note, principalId);
        return ResponseEntity.noContent().build();
    }

    // content type is not set because it's not necessary
    @DeleteMapping(value = "/{noteId}")
    public ResponseEntity<Void> delete(@PathVariable Long noteId, @MyId Long principalId) {
        noteService.delete(noteId, principalId);
        return ResponseEntity.noContent().build();
    }
}
