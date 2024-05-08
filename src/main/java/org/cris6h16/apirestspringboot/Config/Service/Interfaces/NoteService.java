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
    ResponseEntity<Void> updateNoteById(Long id, @Valid @NotNull CreateNoteDTO note); // @valid because is PUT not PATCH, then all fields are required
    ResponseEntity<Void> deleteNoteById(Long id);
}