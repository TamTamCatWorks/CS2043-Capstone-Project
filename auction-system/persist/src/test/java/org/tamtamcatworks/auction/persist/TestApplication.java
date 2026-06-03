package org.tamtamcatworks.auction.persist;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan("org.tamtamcatworks.auction")
@EnableJpaRepositories("org.tamtamcatworks.auction.persist.repository")
public class TestApplication {
}
