package org.tamtamcatworks.auction.service.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.tamtamcatworks.auction.persist.repository.UserRepository;

import java.util.Optional;

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
        UserRepository.UserAuthView userAuthView = new UserRepository.UserAuthView() {
            @Override
            public String getEmail() {
                return "user@example.com";
            }

            @Override
            public String getPasswordHash() {
                return "encoded-password";
            }
        };
        when(userRepository.findAuthByEmail("user@example.com"))
            .thenReturn(Optional.of(userAuthView));

        UserDetails userDetails = authUserDetailsService.loadUserByUsername("user@example.com");

        assertEquals("user@example.com", userDetails.getUsername());
        assertEquals("encoded-password", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsernameThrowsWhenUserIsMissing() {
        when(userRepository.findAuthByEmail("missing@example.com"))
            .thenReturn(Optional.empty());

        assertThrows(
            UsernameNotFoundException.class,
            () -> authUserDetailsService.loadUserByUsername("missing@example.com")
        );
    }
}
