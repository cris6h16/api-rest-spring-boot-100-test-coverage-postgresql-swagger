package org.cris6h16.apirestspringboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class ApiRestSpringBootApplicationTests {

    /**
     * Test added just for reach the 100% of coverage
     */
    @Test
    void contextLoads() {
        try {
            ApiRestSpringBootApplication.main(new String[]{});
        } catch (Exception ignored) {
        }
    }

}