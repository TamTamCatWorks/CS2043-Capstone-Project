package org.tamtamcatworks.auction.persist.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.tamtamcatworks.auction.model.user.User;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindOperations() {
        User user = new User("alice", "alice@example.com", "pass123", "Alice Smith", 500.0);
        userRepository.save(user);

        Optional<User> foundByUsername = userRepository.findByUsername("alice");
        assertTrue(foundByUsername.isPresent());
        assertEquals("Alice Smith", foundByUsername.get().getFullName());

        Optional<User> foundByEmail = userRepository.findByEmail("alice@example.com");
        assertTrue(foundByEmail.isPresent());
        assertEquals("alice", foundByEmail.get().getUsername());

        assertTrue(userRepository.existsByUsername("alice"));
        assertFalse(userRepository.existsByUsername("bob"));

        assertTrue(userRepository.existsByEmail("alice@example.com"));
        assertFalse(userRepository.existsByEmail("bob@example.com"));
    }

    @Test
    void testFindAuthByEmailProjection() {
        User user = new User("bob", "bob@example.com", "securehash", "Bob Jones", 100.0);
        userRepository.save(user);

        Optional<UserRepository.UserAuthView> authViewOpt = userRepository.findAuthByEmail("bob@example.com");
        assertTrue(authViewOpt.isPresent());
        UserRepository.UserAuthView authView = authViewOpt.get();
        assertEquals("bob@example.com", authView.getEmail());
        assertEquals("securehash", authView.getPasswordHash());
    }
}
