package org.cris6h16.apirestspringboot.Config.Service.Interfaces;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface NoteService {
    ResponseEntity<Void> createNote(@Valid @NotNull CreateNoteDTO note);
    ResponseEntity<List<PublicNoteDTO>> getPage(Pageable pageable);
    ResponseEntity<PublicNoteDTO> getNoteById(Long id);
    ResponseEntity<Void> updateNoteById(UpdateNoteDTO note);
}