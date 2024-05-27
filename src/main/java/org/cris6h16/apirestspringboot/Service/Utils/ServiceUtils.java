package org.cris6h16.apirestspringboot.Service.Utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.AbstractServiceExceptionWithStatus;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.Common.InvalidIdException;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.NoteServiceTraversalException;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserService.UserNotFoundException;
import org.cris6h16.apirestspringboot.Exceptions.service.WithStatus.UserServiceTraversalException;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.cris6h16.apirestspringboot.Constants.Cons.User.Constrains.*;

@Slf4j
@Component
public class ServiceUtils {

    UserRepository userRepository;

    public ServiceUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AbstractServiceExceptionWithStatus createATraversalExceptionHandled(@NotNull Exception e, boolean isUserService) {
        String forClient = ""; // PD: verification based on: .isBlank(), dont add generic message here
        HttpStatus recommendedStatus = null; // also here, but with null

        // ------------ commons in both entities --------------------\\
        // data integrity violations { not blank, invalid email, max length, etc }
        if (e instanceof ConstraintViolationException && forClient.isBlank()) {
            recommendedStatus = HttpStatus.BAD_REQUEST;
            Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) e).getConstraintViolations();

            if (!violations.isEmpty()) forClient = violations.iterator().next().getMessage();
            else log.error("ConstraintViolationException: {}", e.getMessage());
        }

        // Note Service: { user not found, invalid id }
        // UserService:  { user not found, invalid id, password too short }
        if (e instanceof AbstractServiceExceptionWithStatus && forClient.isBlank()) {
            recommendedStatus = ((AbstractServiceExceptionWithStatus) e).getRecommendedStatus();
            forClient = e.getMessage();
        }


        // ------------ response regard to the entity ------------- \\
        if (isUserService) {
            // unique violations { primary key, unique constraints }
            if (e instanceof DuplicateKeyException && forClient.isBlank()) {
                recommendedStatus = HttpStatus.CONFLICT;
                boolean inUsername = thisContains(e.getMessage(), USERNAME_UNIQUE_NAME);
                boolean inEmail = thisContains(e.getMessage(), EMAIL_UNIQUE_NAME);
                boolean isHandledUniqueViolation = inUsername || inEmail;

                if (isHandledUniqueViolation) forClient = inUsername ? USERNAME_UNIQUE_MSG : EMAIL_UNIQUE_MSG;
                else log.error("DuplicateKeyException: {}", e.getMessage());
            }

        } else { // ------- is Note Service ------- \\
            // now for note service I don't have custom exceptions
        }


        // unhandled exceptions -> generic error
        if (forClient.isBlank()) {
            if (recommendedStatus == null) recommendedStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            forClient = Cons.Response.ForClient.GENERIC_ERROR;
            log.error("Unhandled exception: {}", e.getMessage());
        }

        return isUserService ?
                new UserServiceTraversalException(forClient, recommendedStatus) :
                new NoteServiceTraversalException(forClient, recommendedStatus);

    }


    public boolean thisContains(String msg, String... strings) {
        boolean contains = true;
        for (String s : strings) contains = contains && msg.contains(s);

        return contains;
    }


    /**
     * Validate id  and get user from repository
     *
     * @param userId to validate
     * @return {@link UserEntity}
     * @throws AbstractServiceExceptionWithStatus if user not found or id is invalid
     */
    public UserEntity validateIdAndGetUser(Long userId) {
        validateId(userId);
        return userRepository
                .findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * @param id to validate
     * @throws AbstractServiceExceptionWithStatus impl if id is null or less than 1
     */
    void validateId(Long id) {
        if (id == null || id <= 0) throw new InvalidIdException();
    }
}
