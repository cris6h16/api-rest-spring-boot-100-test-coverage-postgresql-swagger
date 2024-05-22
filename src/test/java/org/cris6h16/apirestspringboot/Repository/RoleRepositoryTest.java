package org.cris6h16.apirestspringboot.Repository;

import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link RoleRepository}.<br>
 * This class uses an embedded {@code H2} database to simulate the real database environment.<br>
 * <p>
 * Using the {@code H2} database provides the following benefits:
 * <ul>
 *   <li>Isolation: Tests run in an isolated environment, ensuring no interference with the real database.</li>
 *   <li>Speed: Embedded databases like H2 execute faster than real databases, speeding up test execution.</li>
 *   <li>Maintenance: There is no need to clean the database manually, even if the database structure changes.</li>
 * </ul>
 * <p>
 * Although you can configure tests to use the actual database, it is not recommended due to potential issues such as:
 * <ul>
 *   <li>Loss of isolation: Tests may interfere with real data, leading to inconsistent results.</li>
 *   <li>Slower execution: Real databases typically perform slower than in-memory databases like H2.</li>
 *   <li>Manual cleanup: Changes in the database structure may require manual cleanup, complicating test maintenance.</li>
 * </ul>
 *
 * @author <a href="https://github.com/cris6h16" target="_blank">Cristian Herrera</a>
 */
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;
    private List<RoleEntity> roles;

    /**
     * Initializes the roles list
     */
    public RoleRepositoryTest() {
        roles = List.of(
                RoleEntity.builder().name(ERole.ROLE_USER).build(),
                RoleEntity.builder().name(ERole.ROLE_ADMIN).build()
        );
    }

    /**
     * <ol>
     *     <li>Deletes all roles from the repository</li>
     *     <li>Saves all roles to the repository</li>
     * </ol>
     */
    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
        roleRepository.saveAll(roles);
    }

    /**
     * Tests for the {@link RoleRepository#findByName(ERole)} method
     */
    @Test
    void findByName() {
        //Arrange
        assertThat(roleRepository.count()).isEqualTo(2);

        //Act
        roles.forEach(role -> {
            RoleEntity fromDB = roleRepository.findByName(role.getName()).orElse(null);

            //Assert
            assertThat(fromDB).isNotNull();
            assertThat(fromDB.getName()).isEqualTo(role.getName());
        });
    }
}