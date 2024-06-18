package org.cris6h16.apirestspringboot.Service.Interfaces;

import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchEmailUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchPasswordUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchUsernameUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service layer for {@link UserRepository}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
public interface UserService {
    /**
     * Create a new user
     *
     * @param dto the user to create
     * @return the id of the created user
     * @throws UserServiceTransversalException with the proper
     *                                         status code and message ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    Long create(CreateUserDTO dto);

    /**
     * Get a user by id
     *
     * @param id of the user to getById
     * @return the user owner of the provided {@code id} as a {@link PublicUserDTO}
     * @throws UserServiceTransversalException with the proper
     *                                         status code and message ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    PublicUserDTO getById(Long id);


    /**
     * Delete a user by id
     *
     * @param id of the user to deleteById
     * @throws UserServiceTransversalException with the proper
     *                                         status code and message ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void deleteById(Long id);

    /**
     * Get all users paginated
     *
     * @param pageable the page request
     * @return a list of {@link PublicUserDTO}
     * @throws UserServiceTransversalException with the proper
     *                                         status code and message ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    List<PublicUserDTO> getPage(Pageable pageable);

    void patchUsernameById(Long id, PatchUsernameUserDTO dto);

    void patchEmailById(Long id, PatchEmailUserDTO dto);

    void patchPasswordById(Long id, PatchPasswordUserDTO dto);
}
