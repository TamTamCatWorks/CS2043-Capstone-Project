package org.tamtamcatworks.auction.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tamtamcatworks.auction.model.user.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    interface UserAuthView {
        String getEmail();
        String getPasswordHash();
    }

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("""
        select u.email as email, u.passwordHash as passwordHash
        from User u
        where u.email = :email
        """)
    Optional<UserAuthView> findAuthByEmail(@Param("email") String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
