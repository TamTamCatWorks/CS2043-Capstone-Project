package org.tamtamcatworks.auction.service.security;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthUserDetailsService authUserDetailsService;

    @Test
    void loadUserByUsernameReturnsUserDetailsForExistingUser() {

        User user = new User(
            "username",
            "user@example.com",
            "encoded-password",
            "Test User",
            0.0
        );

        when(userRepository.findByEmail("user@example.com"))
            .thenReturn(Optional.of(user));

        UserDetails userDetails =
            authUserDetailsService.loadUserByUsername(
                "user@example.com"
            );

        assertEquals(
            "user@example.com",
            userDetails.getUsername()
        );

        assertEquals(
            "encoded-password",
            userDetails.getPassword()
        );

        assertEquals(
            1,
            userDetails.getAuthorities().size()
        );

        assertEquals(
            "ROLE_USER",
            userDetails
                .getAuthorities()
                .iterator()
                .next()
                .getAuthority()
        );
    }

    @Test
    void loadUserByUsernameThrowsWhenUserIsMissing() {

        when(userRepository.findByEmail("missing@example.com"))
            .thenReturn(Optional.empty());

        assertThrows(
            UsernameNotFoundException.class,
            () -> authUserDetailsService.loadUserByUsername(
                "missing@example.com"
            )
        );
    }
}