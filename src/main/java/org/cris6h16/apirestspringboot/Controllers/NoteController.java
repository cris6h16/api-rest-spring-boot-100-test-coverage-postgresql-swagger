package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Controllers.MetaAnnotations.MyId;
import org.cris6h16.apirestspringboot.Service.NoteServiceImpl;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

//@RestController
@Controller
@ResponseBody
@RequestMapping(path = NoteController.path)
@PreAuthorize("isAuthenticated()")
public class NoteController {

    public static final String path = "/api/notes";
    private final NoteServiceImpl noteService;

    public NoteController(NoteServiceImpl noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody CreateNoteDTO note, @MyId Long principalId) {
        Long id = noteService.create(note, principalId);
        URI uri = URI.create(path + "/" + id);

        return ResponseEntity.created(uri).build();
    }

    @GetMapping
    public ResponseEntity<List<PublicNoteDTO>> getPage(Pageable pageable, @MyId Long principalId) {
        List<PublicNoteDTO> list = noteService.getPage(pageable, principalId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<PublicNoteDTO> get(@PathVariable Long noteId, @MyId Long principalId) {
        PublicNoteDTO en = noteService.get(noteId, principalId);
        return ResponseEntity.ok(en);
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<Void> update(@PathVariable Long noteId,
                                       @RequestBody CreateNoteDTO note,
                                       @MyId Long principalId) {
        noteService.put(noteId, note, principalId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> delete(@PathVariable Long noteId, @MyId Long principalId) {
        noteService.delete(noteId, principalId);
        return ResponseEntity.noContent().build();
    }
}
