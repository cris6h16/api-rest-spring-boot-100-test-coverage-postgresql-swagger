package org.cris6h16.apirestspringboot.Controllers;

import jakarta.validation.Valid;
import org.cris6h16.apirestspringboot.Config.Service.UserService;
import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

//@RestController
@Controller
@ResponseBody
@RequestMapping("/api/users")
public class UserController {

    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //TODO: doc about how a ResponseEntity which is Void can contain a body when an exception is threw

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> createUser(@RequestBody CreateUserDTO user) {
        return userService.createUser(user);
    }

    @GetMapping("/{username}")
    @PreAuthorize("#username == authentication.principal.username") // SpEL
    public ResponseEntity<PublicUserDTO> getUser(@PathVariable String username) {
        return userService.getByUsername(username);
    }
}
