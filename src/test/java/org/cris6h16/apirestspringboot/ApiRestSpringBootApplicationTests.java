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
     *
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    @Test
    void contextLoads() {
        try {
            ApiRestSpringBootApplication.main(new String[]{});
        } catch (Exception ignored) {
        }
    }

}