package org.cris6h16.apirestspringboot.Service.Interfaces;

import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    Long create(CreateUpdateUserDTO dto);
    PublicUserDTO get(Long id);
    void update(Long id, CreateUpdateUserDTO dto);
    void delete(Long id);
    List<PublicUserDTO> get(Pageable pageable);

    // todo: delete notes in userentity for see what happens( i consider that note attribite isn't necessary)
}
