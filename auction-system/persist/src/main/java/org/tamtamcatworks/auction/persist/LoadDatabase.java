package org.tamtamcatworks.auction.persist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.tamtamcatworks.auction.model.user.BuyerProfile;
import org.tamtamcatworks.auction.model.user.SellerProfile;
import org.tamtamcatworks.auction.model.user.User;
import org.tamtamcatworks.auction.persist.repository.UserRepository;

@Configuration
public class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            User user = new User("testuser", "test@example.com", "hashed123", "Test User", 1000.0);
            user.setBuyerProfile(new BuyerProfile());
            user.setSellerProfile(new SellerProfile());

            log.info("Preloading " + userRepository.save(user));
        };
    }
}