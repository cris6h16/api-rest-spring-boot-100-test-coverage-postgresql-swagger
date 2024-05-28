package org.cris6h16.apirestspringboot.deleteThis;

import org.cris6h16.apirestspringboot.Service.Interfaces.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
@Transactional(rollbackFor = Exception.class)
public class pruebas {
    @Autowired
    private UserService userService;

    @Test
    public void test() {
        try {
            userService.get(
                    PageRequest.of(1, 1,
                            Sort.by(Sort.Direction.ASC, "cris6h16"))
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
