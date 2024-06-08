package org.cris6h16.apirestspringboot.Entities;

import jakarta.validation.ConstraintViolationException;
import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for {@link RoleEntity} validations and constraints<br>
 *
 * @author <a href="github.com/cris6h16" target="_blank">Cristian Herrera</a>
 */
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional(rollbackFor = Exception.class)
public class RoleConstrainsValidationsTest {
    @Autowired
    private RoleRepository roleRepository;
    @Test
    void RoleConstrainsValidationsTest_nameIsNull_thenThrowsConstraintViolationException(){
        // Arrange
        RoleEntity role = RoleEntity.builder()
                .name(null)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> roleRepository.saveAndFlush(role))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining( Cons.Role.Validations.NAME_IS_BLANK);
    }

}
