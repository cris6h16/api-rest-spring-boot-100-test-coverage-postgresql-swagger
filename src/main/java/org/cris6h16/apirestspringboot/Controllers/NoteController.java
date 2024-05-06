package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Config.Service.NoteServiceImpl;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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
}
