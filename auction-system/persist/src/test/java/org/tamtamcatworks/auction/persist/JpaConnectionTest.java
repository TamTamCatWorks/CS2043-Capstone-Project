package org.tamtamcatworks.auction.persist;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
public class JpaConnectionTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void testConnection() {
        String db = jdbcTemplate.queryForObject("SELECT current_schema()", String.class);
        System.out.println("Connected to schema: " + db);
    }
}