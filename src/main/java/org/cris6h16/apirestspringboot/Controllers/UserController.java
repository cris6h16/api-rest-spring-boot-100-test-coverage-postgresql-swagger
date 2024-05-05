package org.cris6h16.apirestspringboot.Controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.cris6h16.apirestspringboot.Config.Security.CustomUser.UserWithId;
import org.cris6h16.apirestspringboot.Config.Service.UserService;
import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.UpdateUserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

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
    public ResponseEntity<Void> createUser(@RequestBody CreateUserDTO user) {
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicUserDTO> getUserById(@PathVariable Long id) { //TODO: Impl passing of Strings -> Test
        return userService.getByIdLazy(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id,
                                           @RequestBody UpdateUserDTO user) {//TODO: Impl passing of NUmbers -> Test
        return userService.updateUser(id, user);
    }
}
