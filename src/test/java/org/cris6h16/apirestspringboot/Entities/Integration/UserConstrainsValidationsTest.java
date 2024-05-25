package org.cris6h16.apirestspringboot.Entities.Integration;

import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatList;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional(rollbackFor = Exception.class)
public class UserConstrainsValidationsTest {
    //    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    private UserEntity usr;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // necessary with h2
        userRepository.flush();
        roleRepository.flush();

        // `usr`
        initializeAndPrepare();
    }

    @Test
    @Order(1)
    @Tag(value = "correct")
    void UserConstrainsValidationsTest_correctInsertion() {
        // Arrange
        // Act
        userRepository.saveAndFlush(usr);
        // Assert
        Iterator<?> i = userRepository.findAll().iterator();
        assertThat(i).asList().hasSize(1);
        assertThat(i.next().equals(usr)).isTrue();
    }

    @Test
    @Order(2)
    @Tag(value = "DataIntegrityViolationException")
    void UserConstrainsValidationsTest_UsernameAlreadyExists() {
        // Arrange
        userRepository.saveAndFlush(usr);
        UserEntity usr2 = UserEntity.builder()
                .username(usr.getUsername())
                .email(usr.getEmail())
                .password("12345678")
                .roles(Set.of(roleRepository.findAll().iterator().next()))
                .build();
        // Act
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(usr2));
    }

    @Test
    @Order(2)
    @Tag(value = "DataIntegrityViolationException")
    void UserConstrainsValidationsTest_EmailAlreadyExists() {
        // Arrange
        userRepository.saveAndFlush(usr);
        UserEntity usr2 = UserEntity.builder()
                .username("github.com/cris6h16")
                .email(usr.getEmail())
                .password("12345678")
                .roles(Set.of(roleRepository.findAll().iterator().next()))
                .build();
        // Act
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(usr2));
    }


    void initializeAndPrepare(){
        RoleEntity roles = RoleEntity.builder().name(ERole.ROLE_USER).build();
        usr = UserEntity.builder()
                .id(null)
                .username("cris6h16")
                .password("12345678")
                .email("cris6h16@gmail.com")
                .roles(Set.of(roles))
                .build();
    }
}
