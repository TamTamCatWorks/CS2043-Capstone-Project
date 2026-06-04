package org.tamtamcatworks.auction.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.tamtamcatworks.auction.persist.repository.UserRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           UserRepository userRepository,
                                           ObjectMapper objectMapper) throws Exception {
        // SuspendedUserFilter runs after the security context is restored from the HTTP session.
        // It is NOT declared as a @Bean to prevent Spring Boot from auto-registering it as
        // a plain Servlet filter (which would apply it outside the security chain as well).
        SuspendedUserFilter suspendedUserFilter =
                new SuspendedUserFilter(userRepository, objectMapper);

        http
            .addFilterAfter(suspendedUserFilter, SecurityContextHolderFilter.class)
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                "/users/register",
                "/users/login",
                "/items",
                "/auctions",
                "/auctions/existing-item",
                "/auctions/*/open",
                "/auctions/*/close",
                "/auctions/*/cancel",
                "/auctions/*/bids",
                "/images/upload",
                "/users/top-up",
                "/notifications",
                "/notifications/*/read",
                "/notifications/read-all",
                "/ws/**",
                "/admin/**",
                "/auctions/*/auto-bid"
            ))

            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .securityContext(context -> context.securityContextRepository(securityContextRepository()))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/authenticate").permitAll()
                .requestMatchers("/users/register", "/users/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/items/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/auctions", "/auctions/*", "/auctions/*/bids").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/notifications/**").authenticated()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
