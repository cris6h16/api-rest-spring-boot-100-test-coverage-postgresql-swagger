package org.cris6h16.apirestspringboot.Service;

import org.cris6h16.apirestspringboot.DTOs.CreateUpdateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.PublicUserDTO;
import org.cris6h16.apirestspringboot.DTOs.RoleDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Repository.NoteRepository;
import org.cris6h16.apirestspringboot.Repository.RoleRepository;
import org.cris6h16.apirestspringboot.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // remember add the dependency
public class UserServiceImplTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private NoteRepository noteRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        userRepository.flush();
        roleRepository.flush();
    }

    @Test
    @Tag("create")
    void ServiceUtils_create_RoleNonexistentInDBCascade_Successful() {
        // Arrange
        CreateUpdateUserDTO dto = createValidDTO();

        // Act
        Long id = userService.create(dto);

        // Assert
        UserEntity user = userRepository.findById(id).orElse(null);
        RoleEntity role = user.getRoles().iterator().next();

        assertThat(user)
                .isNotNull()
                .hasFieldOrPropertyWithValue("username", dto.getUsername())
                .hasFieldOrPropertyWithValue("email", dto.getEmail());
        assertThat(passwordEncoder.matches(dto.getPassword(), user.getPassword()))
                .isTrue();

        assertThat(role)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", ERole.ROLE_USER)
                .hasNoNullFieldsOrPropertiesExcept("id");
    }


    @Test
    @Tag("create")
    void ServiceUtils_create_RoleExistentInDBThenAssignIt_Successful() {
        // Arrange
        CreateUpdateUserDTO dto = createValidDTO();
        RoleEntity r = roleRepository.saveAndFlush(RoleEntity.builder().name(ERole.ROLE_USER).build());

        // Act
        Long id = userService.create(dto);

        // Assert
        UserEntity user = userRepository.findById(id).orElse(null);
        assertThat(user)
                .isNotNull()
                .hasFieldOrPropertyWithValue("roles", Set.of(r))
                .hasFieldOrPropertyWithValue("username", dto.getUsername())
                .hasFieldOrPropertyWithValue("email", dto.getEmail());
        assertThat(passwordEncoder.matches(dto.getPassword(), user.getPassword())).isTrue();
    }

    private CreateUpdateUserDTO createValidDTO() {
        return CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cris6h16@gmail.com")
                .build();
    }

    @Test
    @Tag("get")
    void ServiceUtils_get_UserFoundWithRolesNull_thenInRolesReturnEmptySet_Successful() {
        // Arrange
        UserEntity usr = UserEntity.builder()
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(null)
                .build();
        userRepository.saveAndFlush(usr);

        // Act
        PublicUserDTO user = userService.get(usr.getId());

        // Assert
        assertThat(user)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", usr.getId())
                .hasFieldOrPropertyWithValue("username", usr.getUsername())
                .hasFieldOrPropertyWithValue("email", usr.getEmail())
                .hasFieldOrPropertyWithValue("roles", new HashSet<>());
    }

    @Test
    @Tag("get")
    @Tag("correct")
    void ServiceUtils_get_UserFoundWithRoles_Successful() {
        // Arrange
        Long userId = 1L;
        RoleEntity role = roleRepository.saveAndFlush(RoleEntity.builder()
                .id(10L)
                .name(ERole.ROLE_USER)
                .build());
        UserEntity createdEntity = userRepository.saveAndFlush(UserEntity.builder()
                .id(userId)
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(Set.of(role))
                .build());

        // Act
        PublicUserDTO dto = userService.get(userId);

        // Assert
        assertThat(dto)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", userId)
                .hasFieldOrPropertyWithValue("username", createdEntity.getUsername())
                .hasFieldOrPropertyWithValue("email", createdEntity.getEmail());

        RoleDTO rDTO = dto.getRoles().iterator().next();
        assertThat(rDTO.getName()).isEqualTo(role.getName());
    }


    @Test
    @Tag("update")
    void ServiceUtils_update_DTO_WantUpdateUsername_Successful() {
        // Arrange
        UserEntity original = UserEntity.builder()
                .username("github.com/cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(new HashSet<>(Collections.singleton(RoleEntity.builder().name(ERole.ROLE_USER).build())))
                .build();
        userRepository.saveAndFlush(original);

        CreateUpdateUserDTO updateUsernameDTO = CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .build();

        // Act
        userService.update(original.getId(), updateUsernameDTO);
        original = userRepository.findById(original.getId()).orElse(null);

        // Assert
        assertThat(original)
                .isNotNull()
                .hasFieldOrPropertyWithValue("username", updateUsernameDTO.getUsername())
                .hasFieldOrPropertyWithValue("email", original.getEmail())
                .hasFieldOrPropertyWithValue("password", original.getPassword())
                .hasFieldOrPropertyWithValue("roles", original.getRoles());
    }

    @Test
    @Tag("update")
    void ServiceUtils_update_DTO_WantUpdateEmail_Successful() {
        // Arrange
        UserEntity original = UserEntity.builder()
                .username("github.com/cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(new HashSet<>(Collections.singleton(RoleEntity.builder().name(ERole.ROLE_USER).build())))
                .build();
        userRepository.saveAndFlush(original);

        CreateUpdateUserDTO updateEmailDTO = CreateUpdateUserDTO.builder()
                .email("cris6h16@example.com")
                .build();

        // Act
        userService.update(original.getId(), updateEmailDTO);
        original = userRepository.findById(original.getId()).orElse(null);

        // Assert
        assertThat(original)
                .isNotNull()
                .hasFieldOrPropertyWithValue("username", original.getUsername())
                .hasFieldOrPropertyWithValue("email", updateEmailDTO.getEmail())
                .hasFieldOrPropertyWithValue("password", original.getPassword())
                .hasFieldOrPropertyWithValue("roles", original.getRoles());
    }

    @Test
    @Tag("update")
    void ServiceUtils_update_DTO_WantUpdatePassword_Successful() {
        // Arrange
        CreateUpdateUserDTO toCReate = CreateUpdateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .build();
        Long id = userService.create(toCReate);

        CreateUpdateUserDTO updatePasswordDTO = CreateUpdateUserDTO.builder()
                .password("cris6h16's password")
                .build();

        // Act
        userService.update(id, updatePasswordDTO);


    }

    @Test
    @Tag("get(pageable)")
    void ServiceUtils_getPageable_ReturnList_Successful() {
        // Arrange
        List<UserEntity> entities = userRepository.saveAllAndFlush(getUserEntities(10));

        int pageNum = 0;
        int pageSize = 17;
        Pageable pageable = PageRequest.of(
                pageNum,
                pageSize,
                Sort.by(Sort.Direction.ASC, "id"));

        // Act
        List<PublicUserDTO> page = userService.get(pageable);

        // Assert
        assertThat(page)
                .isNotNull()
                .hasSize(entities.size())
                .isSortedAccordingTo(Comparator.comparing(PublicUserDTO::getId));

    }

    private List<UserEntity> getUserEntities(int amount) {
        List<UserEntity> entities = new ArrayList<>();
        RoleEntity role = roleRepository.findByName(ERole.ROLE_USER).orElse(
                RoleEntity.builder().name(ERole.ROLE_USER).build());
        for (int i = 0; i < amount; i++) {
            entities.add(UserEntity.builder()
                    .username("cris6h16" + i)
                    .email(i + "cristianmherrera21@gmail.com")
                    .password("{bcrypt}$2a81..." + i)
                    .roles(new HashSet<>(Collections.singleton(role)))
                    .createdAt(new Date())
                    .build());
        }
        return entities;
    }

}
