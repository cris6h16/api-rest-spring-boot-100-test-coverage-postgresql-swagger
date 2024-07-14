package org.cris6h16.apirestspringboot.Controllers.UserController;

import jakarta.validation.Valid;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.Services.Interfaces.UserService;
import org.cris6h16.apirestspringboot.Services.UserServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping(PublicUserController.path)
public class PublicUserController {

    public static final String path = Cons.User.Controller.Path.USER_PATH;


    UserServiceImpl userService;

    public PublicUserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    /**
     * Create a new user<br>
     * make it using: {@link UserService#create(CreateUserDTO)}
     *
     * @param user {@link CreateUserDTO} with the data of the new user
     * @return {@link ResponseEntity#created(URI)} with the location of the new user
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> create(@RequestBody(required = true) CreateUserDTO user) {
        Long id = userService.create(user);
        URI uri = URI.create(path + "/" + id);
        return ResponseEntity.created(uri).build();
    }
}
