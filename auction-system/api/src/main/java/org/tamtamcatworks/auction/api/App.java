package org.tamtamcatworks.auction.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "org.tamtamcatworks.auction")
@EntityScan("org.tamtamcatworks.auction")
@EnableJpaRepositories("org.tamtamcatworks.auction")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}