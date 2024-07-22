package org.cris6h16.apirestspringboot.Services.Interfaces;

import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchEmailUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchPasswordUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchUsernameUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * @param dto   the data of the new user
     * @param roles the roles of the new user
     * @return the id of the new user created
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    Long create(CreateUserDTO dto, ERole... roles);

    /**
     * Get a user by id
     *
     * @param id of the user to get
     * @return a {@link PublicUserDTO} with the user data
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    PublicUserDTO getById(Long id);


    /**
     * Delete a user by id
     *
     * @param id of the user to delete
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void deleteById(Long id);

    /**
     * Get a page of users
     *
     * @param pageable the page request
     * @return a list of {@link PublicUserDTO} with the users data
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    Page<PublicUserDTO> getPage(Pageable pageable);

    /**
     * Patch the username of a user by id
     *
     * @param id  of the user to patch
     * @param dto with the new username
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void patchUsernameById(Long id, PatchUsernameUserDTO dto);

    /**
     * Patch the email of a user by id
     *
     * @param id  of the user to patch
     * @param dto with the new email
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void patchEmailById(Long id, PatchEmailUserDTO dto);

    /**
     * Patch the password of a user by id
     *
     * @param id  of the user to patch
     * @param dto with the new password
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void patchPasswordById(Long id, PatchPasswordUserDTO dto);

    /**
     * Delete all users
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void deleteAll();

}
