package org.cris6h16.apirestspringboot.Controllers;

import jakarta.persistence.PostRemove;
import jakarta.validation.Valid;
import org.cris6h16.apirestspringboot.Config.Service.NoteService;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

//@RestController
@Controller
@ResponseBody
@RequestMapping("/api/notes")
public class NoteController {
    NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<Void> createNote(@RequestBody CreateNoteDTO note) {
        return noteService.createNote(note);
    }

}
