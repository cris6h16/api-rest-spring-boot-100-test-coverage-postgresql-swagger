package org.cris6h16.apirestspringboot.Service.Interfaces;

import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserServiceTransversalException;
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
    Long create(CreateUpdateUserDTO dto);

    /**
     * Get a user by id
     *
     * @param id of the user to get
     * @return the user owner of the provided {@code id} as a {@link PublicUserDTO}
     * @throws UserServiceTransversalException with the proper
     *                                         status code and message ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    PublicUserDTO get(Long id);

    /**
     * Update user fields, just the field that we want to update
     * must contain the new value, the rest fields should be {@code null}
     * <p>
     * Examples:
     * <ul>
     *     <li>
     *         <b>update a username:</b><br>
     *         {@code username: "newName", password: null, email: null ...}
     *     </li>
     *     <li>
     *         <b>update a password:</b><br>
     *         {@code username: null, password: "newPassword", email: null ...}
     *     </li>
     * </ul>
     * </p>
     *
     * @param id  of the user to update
     * @param dto containing the new values for the user
     * @throws UserServiceTransversalException with the proper
     *                                         status code and message ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void update(Long id, CreateUpdateUserDTO dto);

    /**
     * Delete a user by id
     *
     * @param id of the user to delete
     * @throws UserServiceTransversalException with the proper
     *                                         status code and message ready to be sent to the client
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    void delete(Long id);

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
    List<PublicUserDTO> get(Pageable pageable);
}
