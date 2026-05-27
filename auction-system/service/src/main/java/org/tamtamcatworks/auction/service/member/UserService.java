package org.tamtamcatworks.auction.service.member;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tamtamcatworks.auction.model.user.BuyerProfile;
import org.tamtamcatworks.auction.model.user.SellerProfile;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.UserRepository;
import org.tamtamcatworks.auction.service.event.UserStateEvent;
import org.tamtamcatworks.auction.service.mapper.UserMapper;
import org.tamtamcatworks.auction.shared.request.RegisterRequest;
import org.tamtamcatworks.auction.shared.response.UserResponse;

import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper,
                       ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public User register(String username, String email, String password, String fullName) {
        validateRegistrationInputs(username, email);

        User user = new User(username, email, passwordEncoder.encode(password), fullName, 0.0);
        user.setBuyerProfile(new BuyerProfile());
        user.setSellerProfile(new SellerProfile());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("Invalid email or password."));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("User not found."));
    }

    @Transactional(readOnly = true)
    public User findById(@NonNull String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("User not found."));
    }

    @Transactional
    public UserResponse registerByRequest(RegisterRequest registerRequest) {
        validateRegistrationInputs(registerRequest.username(), registerRequest.email());

        User user = userMapper.toEntity(registerRequest, passwordEncoder.encode(registerRequest.password()));
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse findResponseById(@NonNull String id) {
        return userMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public UserResponse findResponseByEmail(@NonNull String email) {
        return userMapper.toResponse(findByEmail(email));
    }

    @Transactional(readOnly = true)
    public UserResponse toResponse(@NonNull User user) {
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse topUp(@NonNull String id, double amount) {
        User user = findById(id);
        user.addBalance(amount);
        userRepository.save(user);
        UserResponse response = userMapper.toResponse(user);
        eventPublisher.publishEvent(new UserStateEvent(id, response));
        return response;
    }

    private void validateRegistrationInputs(String username, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken.");
        }
    }
}
