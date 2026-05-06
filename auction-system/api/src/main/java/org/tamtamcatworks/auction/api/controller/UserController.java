package org.tamtamcatworks.auction.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.tamtamcatworks.auction.service.member.UserService;

import org.tamtamcatworks.auction.api.dto.UserResponse;
import org.tamtamcatworks.auction.api.dto.RegisterRequest;
import org.tamtamcatworks.auction.api.dto.LoginRequest;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(UserResponse.from(
            userService.register(req.username(), req.email(), req.password(), req.fullName())
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(UserResponse.from(
            userService.login(req.email(), req.password())
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String id) {
        return ResponseEntity.ok(UserResponse.from(userService.findById(id)));
    }
}
