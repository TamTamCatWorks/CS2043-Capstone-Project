package org.tamtamcatworks.auction.service.member;

import org.tamtamcatworks.auction.persist.repository.UserRepository;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.model.user.BuyerProfile;
import org.tamtamcatworks.auction.model.user.SellerProfile;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String username, String email, String password, String fullName) {
        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException("Email already in use.");
        if (userRepository.existsByUsername(username))
            throw new IllegalArgumentException("Username already taken.");

        User user = new User(username, email, passwordEncoder.encode(password), fullName, 0.0);
        user.setBuyerProfile(new BuyerProfile());
        user.setSellerProfile(new SellerProfile());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("Invalid email or password."));
        if (!passwordEncoder.matches(password, user.getPasswordHash()))
            throw new IllegalArgumentException("Invalid email or password.");
        return user;
    }

    @Transactional(readOnly = true)
    public User findById(@NonNull String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("User not found."));
    }
}