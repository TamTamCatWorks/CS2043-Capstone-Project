package org.tamtamcatworks.auction.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import org.tamtamcatworks.auction.service.member.UserService;

import org.tamtamcatworks.auction.api.dto.UserResponse;
import org.tamtamcatworks.auction.api.dto.RegisterRequest;
import org.tamtamcatworks.auction.api.dto.LoginRequest;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public UserController(
        UserService userService,
        AuthenticationManager authenticationManager,
        SecurityContextRepository securityContextRepository
    ) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(UserResponse.from(
            userService.register(req.username(), req.email(), req.password(), req.fullName())
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
        @RequestBody LoginRequest req,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        request.getSession(true);
        request.changeSessionId();
        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.ok(UserResponse.from(userService.findByEmail(req.email())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable @NonNull String id) {
        return ResponseEntity.ok(UserResponse.from(userService.findById(id)));
    }
}
