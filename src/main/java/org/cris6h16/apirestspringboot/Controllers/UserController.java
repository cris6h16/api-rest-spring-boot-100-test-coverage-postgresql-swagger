package org.cris6h16.apirestspringboot.Controllers;

import org.cris6h16.apirestspringboot.Config.Service.UserServiceImpl;
import org.cris6h16.apirestspringboot.DTOs.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicNoteDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.UpdateUserDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@RestController
@Controller
@ResponseBody
@RequestMapping("/api/users")
public class UserController {

    UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    @GetMapping
    public ResponseEntity<List<PublicUserDTO>> getUsers(Pageable pageable) {
        return userService.getUsers(pageable);
    }
}
