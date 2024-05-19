package org.cris6h16.apirestspringboot.Service.Interfaces;

import jakarta.validation.constraints.NotNull;
import org.cris6h16.apirestspringboot.DTOs.CreateNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * @author <a href="github.com/cris6h16" target="_blank"> cris6h16 </a>
 */
public interface NoteService {
    PublicNoteDTO get(Long noteId, Long userId);
    Long create(CreateNoteDTO note, Long userId);
    void put(Long noteId, CreateNoteDTO note, Long userId);
    void delete(Long noteId, Long userId);
    List<PublicNoteDTO> getPage(Pageable pageable, Long userId);

}
