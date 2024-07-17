package org.cris6h16.apirestspringboot.Entities;

import org.cris6h16.apirestspringboot.Repositories.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for {@link RoleEntity} validations and constraints<br>
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@Transactional(rollbackFor = Exception.class)
public class RoleConstrainsValidationsTest {
    @Autowired
    private RoleRepository roleRepository;


    @Test
    void nameIsNull_thenThrowsDataIntegrityViolationException() {
        // Arrange
        RoleEntity role = RoleEntity.builder()
                .name(null)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> roleRepository.saveAndFlush(role))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("null value in column \"name\" of relation \"roles\" violates not-null constraint");
    }

}
