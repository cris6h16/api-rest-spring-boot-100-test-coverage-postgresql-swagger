package org.cris6h16.apirestspringboot.Config.Service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.Entities.NoteEntity;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class NoteService {
    NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @PreAuthorize("@AuthCustomResponses.checkIfIsAuthenticated()")
    public ResponseEntity<Void> createNote(@Valid @NotNull CreateNoteDTO note) {
        NoteEntity noteEntity = NoteEntity.builder()
                .title(note.getTitle())
                .content(note.getContent())
                .build();

        noteRepository.save(noteEntity);

        return ResponseEntity.created(URI.create("/api/notes/" + noteEntity.getId()))
                .build();
    }
}
