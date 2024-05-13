package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Service.NoteServiceImpl;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@RestController
@Controller
@ResponseBody
@RequestMapping("/api/notes")
public class NoteController {
    NoteServiceImpl noteService;

    public NoteController(NoteServiceImpl noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<Void> createNote(@RequestBody CreateNoteDTO note) {
        return noteService.createNote(note);
    }

    @GetMapping
    public ResponseEntity<List<PublicNoteDTO>> getPage(Pageable pageable) {
        return noteService.getPage(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicNoteDTO> getNoteById(@PathVariable Long id) {
        return noteService.getNoteById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateNote(@PathVariable Long id,
                                           @RequestBody CreateNoteDTO note) {
        return noteService.updateNoteById(id, note);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoteById(@PathVariable Long id) {
        return noteService.deleteNoteById(id);
    }
}
