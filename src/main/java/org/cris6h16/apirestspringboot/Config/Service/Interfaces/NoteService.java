package org.cris6h16.apirestspringboot.Config.Service.Interfaces;

import jakarta.persistence.SecondaryTable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

public interface NoteService {
    ResponseEntity<Void> createNote(@Valid @NotNull CreateNoteDTO note);
    ResponseEntity<List<PublicNoteDTO>> getPage(Pageable pageable);
    ResponseEntity<PublicNoteDTO> getNoteById(Long id);
}