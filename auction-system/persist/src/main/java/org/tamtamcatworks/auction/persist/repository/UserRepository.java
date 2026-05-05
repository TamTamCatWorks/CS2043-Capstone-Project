package org.tamtamcatworks.auction.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tamtamcatworks.auction.model.user.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}