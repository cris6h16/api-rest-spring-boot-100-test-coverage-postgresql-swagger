package org.cris6h16.apirestspringboot.Controllers;

import jakarta.validation.Valid;
import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.springframework.http.ResponseEntity;
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

    @PostMapping()
    public ResponseEntity<?> createUser(@RequestBody @Valid CreateUserDTO user) {

        return ResponseEntity.created(null).build();
    }
}
