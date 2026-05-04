package org.tamtamcatworks.auction.persist;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootTest
public class MongoConnectionTest {

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void testConnection() {
        System.out.println("Connected to: " + mongoTemplate.getDb().getName());
    }
}
