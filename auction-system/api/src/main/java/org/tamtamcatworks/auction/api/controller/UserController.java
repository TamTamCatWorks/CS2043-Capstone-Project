package org.tamtamcatworks.auction.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.service.member.UserService;
import org.tamtamcatworks.auction.shared.request.LoginRequest;
import org.tamtamcatworks.auction.shared.request.RegisterRequest;
import org.tamtamcatworks.auction.shared.response.UserResponse;

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
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(userService.registerByRequest(req));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
        @RequestBody LoginRequest req,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        Authentication authenticationRequest =
            UsernamePasswordAuthenticationToken.unauthenticated(req.email(), req.password());
        Authentication authenticationResponse =
            authenticationManager.authenticate(authenticationRequest);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationResponse);


        request.getSession(true);
        request.changeSessionId();

        User user = userService.findByEmail(req.email());
        request.getSession().setAttribute("userId", user.getId());
        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.ok(userService.toResponse(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable @NonNull String id) {
        return ResponseEntity.ok(userService.findResponseById(id));
    }
}
