package org.tamtamcatworks.auction.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.tamtamcatworks.auction.persist.repository.UserRepository;

import java.io.IOException;
import java.util.Map;

/**
 * Security filter that rejects every HTTP request made by a suspended user with
 * {@code 403 Forbidden} before it reaches any controller or service layer.
 *
 * <p>Runs <em>after</em> the Spring Security context is restored from the HTTP session
 * (i.e., after {@code SecurityContextHolderFilter}), so the {@link SecurityContextHolder}
 * is already populated when this filter executes.
 *
 * <p>This class is <strong>not</strong> annotated with {@code @Component} to prevent
 * Spring Boot from auto-registering it as a plain Servlet filter (which would apply it
 * twice — once outside and once inside the security filter chain).
 * Instantiation is done explicitly inside {@link WebSecurityConfig#filterChain}.
 */
public class SuspendedUserFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public SuspendedUserFilter(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (isAuthenticatedUser(auth)) {
            // auth.getName() returns the principal name, which AuthUserDetailsService sets to the user's email
            String email = auth.getName();

            boolean isSuspended = userRepository.findByEmail(email)
                    .map(user -> !user.isActive())
                    .orElse(false);

            if (isSuspended) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Account suspended"));
                return; // halt — do NOT continue the filter chain
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Returns {@code true} only for fully authenticated non-anonymous principals.
     * Unauthenticated and anonymous requests pass through unchecked.
     */
    private boolean isAuthenticatedUser(Authentication auth) {
        return auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }
}
