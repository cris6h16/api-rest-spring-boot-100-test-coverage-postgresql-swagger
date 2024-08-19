package org.cris6h16.apirestspringboot.Services;

import org.cris6h16.apirestspringboot.Constants.Cons;
import org.cris6h16.apirestspringboot.DTOs.Creation.CreateUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchEmailUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchPasswordUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Patch.PatchUsernameUserDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicRoleDTO;
import org.cris6h16.apirestspringboot.DTOs.Public.PublicUserDTO;
import org.cris6h16.apirestspringboot.Entities.ERole;
import org.cris6h16.apirestspringboot.Entities.RoleEntity;
import org.cris6h16.apirestspringboot.Entities.UserEntity;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.Common.InvalidIdException;
import org.cris6h16.apirestspringboot.Exceptions.WithStatus.service.UserService.*;
import org.cris6h16.apirestspringboot.Repositories.RoleRepository;
import org.cris6h16.apirestspringboot.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


/**
 * Test class for {@link UserServiceImpl}
 *
 * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
public class UserServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        Mockito.reset(roleRepository, userRepository, passwordEncoder);
    }

    @Test
    @Tag("create")
    void create_RoleNonexistentInDB_ThenCreateBoth_Successful() {
        // Arrange
        UserEntity user = createUserEntityWithIdAndRolesWithId();
        CreateUserDTO dtoToCreate = createValidDTO();

        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("{bcrypt}$2a81...");
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenReturn(user);

        // Act
        Long id = userService.create(dtoToCreate, ERole.ROLE_USER);

        // Assert
        assertThat(id).isEqualTo(user.getId());
        verify(roleRepository).findByName(ERole.ROLE_USER);
        verify(passwordEncoder).encode(dtoToCreate.getPassword());
        verify(userRepository).saveAndFlush(argThat(passedToDb ->
                passedToDb.getUsername().equals(dtoToCreate.getUsername()) &&
                        passedToDb.getEmail().equals(dtoToCreate.getEmail()) &&
                        passedToDb.getPassword().equals("{bcrypt}$2a81...") &&
                        passedToDb.getRoles().size() == 1 &&
                        passedToDb.getRoles().iterator().next().getName().equals(ERole.ROLE_USER)));
    }


    @Test
    @Tag("create")
    void create_RoleExistentInDBThenAssignIt_Successful() {
        // Arrange
        UserEntity user = createUserEntityWithIdAndRolesWithId();
        RoleEntity role = user.getRoles().iterator().next();

        when(roleRepository.findByName(ERole.ROLE_USER))
                .thenReturn(Optional.ofNullable(role));
        when(passwordEncoder.encode(any(String.class)))
                .thenReturn("{bcrypt}$2a81...");
        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenReturn(user);

        CreateUserDTO dtoToCreate = createValidDTO();

        // Act
        userService.create(dtoToCreate, ERole.ROLE_USER);

        // Assert
        verify(userRepository).saveAndFlush(argThat(passedToDb ->
                passedToDb.getUsername().equals(dtoToCreate.getUsername()) &&
                        passedToDb.getEmail().equals(dtoToCreate.getEmail()) &&
                        passedToDb.getPassword().equals("{bcrypt}$2a81...") &&
                        passedToDb.getRoles().size() == 1 &&
                        passedToDb.getCreatedAt() != null &&
                        passedToDb.getUpdatedAt() == null &&
                        passedToDb.getRoles().iterator().next().getId().equals(role.getId()) &&
                        passedToDb.getRoles().iterator().next().getName().equals(role.getName())
        ));
    }

    @Test
    @Tag("create")
    void create_withMultiplesRoles_Successful() {
        // Arrange
        UserEntity user = createUserEntityWithIdAndRolesWithId(); // ignored
        ERole[] eRoles = ERole.values();

        when(roleRepository.findByName(any()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class)))
                .thenReturn("{bcrypt}$2a81...");
        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenReturn(user);

        CreateUserDTO dtoToCreate = createValidDTO();

        // Act
        userService.create(dtoToCreate, eRoles);

        // Assert
        verify(userRepository).saveAndFlush(argThat(passedToDb -> {

                    ERole[] rolesPassedToDb = passedToDb.getRoles().stream()
                            .map(RoleEntity::getName)
                            .toArray(ERole[]::new);

                    return passedToDb.getUsername().equals(dtoToCreate.getUsername()) &&
                            passedToDb.getEmail().equals(dtoToCreate.getEmail()) &&
                            passedToDb.getPassword().equals("{bcrypt}$2a81...") &&
                            passedToDb.getCreatedAt() != null &&
                            passedToDb.getUpdatedAt() == null &&
                            passedToDb.getRoles().size() == ERole.values().length &&
                            rolesPassedToDb.length == ERole.values().length &&
                            equalsIgnoreOrder(rolesPassedToDb, eRoles);
                }
        ));
    }

    public static boolean equalsIgnoreOrder(ERole[] arr1, ERole[] arr2) {
        if (arr1 == null || arr2 == null) return arr1 == arr2;
        if (arr1.length != arr2.length) return false;

        // set store unordered the elements, so we can use them to compare
        Set<ERole> set1 = new HashSet<>(Arrays.asList(arr1));
        Set<ERole> set2 = new HashSet<>(Arrays.asList(arr2));
        return set1.equals(set2);
    }

    //    @Test
    @Tag("create")
    @ParameterizedTest
    @ValueSource(strings = {"null", "empty"})
    void create_rolesNullOrEmpty_ThenIllegalArgumentException(String role) {
        // Arrange
        CreateUserDTO dtoToCreate = createValidDTO();
        ERole[] roles = role.equals("null") ? null : new ERole[0];

        // Act & Assert
        assertThatThrownBy(() -> userService.create(dtoToCreate, roles))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @Tag("create")
    void create_nullDTO_ThenAnyUserDTOIsNullException() {
        // Arrange
        CreateUserDTO dtoToCreate = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.create(dtoToCreate, ERole.ROLE_USER))
                .isInstanceOf(AnyUserDTOIsNullException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.DTO.ANY_RELATED_DTO_WITH_USER_NULL)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).saveAndFlush(any());
    }



    @Test
    @Tag("create")
    void create_TrimAndLowerFields() {
        // Arrange
        CreateUserDTO dtoToCreate = CreateUserDTO.builder()
                .username("  Cris6H16  ")
                .password("  12345678  ")
                .email("    cristianmHErrera21@gmail.com ")
                .build();

        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("{bcrypt}$2a81...");
        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenReturn(createUserEntityWithIdAndRolesWithId());

        // Act
        userService.create(dtoToCreate, ERole.ROLE_USER);

        // Assert
        verify(passwordEncoder).encode("12345678");
        verify(userRepository).saveAndFlush(argThat(passedToDb ->
                passedToDb.getUsername().equals("cris6h16") &&
                        passedToDb.getEmail().equals("cristianmherrera21@gmail.com") &&
                        passedToDb.getPassword().equals("{bcrypt}$2a81..."))
        );
    }



    @Tag("create")
    @ParameterizedTest
    @ValueSource(strings = {"null", "blank", "empty", "tooShort", "tooLong"})
    void create_usernameNullOrBlankOrEmptyOrTooShortOrTooLong_ThenUsernameLengthException(String str) {
        // Arrange
        String username = switch (str) {
            case "null" -> null;
            case "blank" -> "   ";
            case "empty" -> "";
            case "tooShort" -> "a".repeat(Cons.User.Validations.MIN_USERNAME_LENGTH - 1);
            case "tooLong" -> "b".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH + 1);
            default -> throw new IllegalStateException("unexpected value: " + str);
        };

        CreateUserDTO dtoToCreate = createValidDTO();
        dtoToCreate.setUsername(username);

        // Act & Assert
        assertThatThrownBy(() -> userService.create(dtoToCreate, new ERole[]{ERole.ROLE_USER, ERole.ROLE_ADMIN}))
                .isInstanceOf(UsernameLengthException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Validations.USERNAME_LENGTH_FAIL_MSG)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).saveAndFlush(any());
    }


    @Tag("create")
    @ParameterizedTest
    @ValueSource(strings = {"null", "blank", "empty", "tooShort", "tooLong", "isNotEmail"})
    void create_emailNullOrBlankOrEmptyOrTooShortOrTooLongOrIsNotEmail_ThenEmailIsInvalidException(String str) {
        // Arrange
        String email = switch (str) {
            case "null" -> null;
            case "blank" -> "   ";
            case "empty" -> "";
            case "tooShort" -> "a".repeat(Cons.User.Validations.MIN_EMAIL_LENGTH - 1);
            case "tooLong" -> "b".repeat(Cons.User.Validations.MAX_EMAIL_LENGTH + 1);
            case "isNotEmail" -> "thisIsNotAnEmail";
            default -> throw new IllegalStateException("unexpected value: " + str);
        };

        CreateUserDTO dtoToCreate = createValidDTO();
        dtoToCreate.setEmail(email);

        // Act & Assert
        assertThatThrownBy(() -> userService.create(dtoToCreate, ERole.ROLE_USER))
                .isInstanceOf(EmailIsInvalidException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Validations.EMAIL_IS_INVALID_MSG)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).saveAndFlush(any());
    }


    @Tag("create")
    @ParameterizedTest
    @ValueSource(strings = {"null", "blank", "empty", "tooShort", "tooLong"})
    void create_passwordNullOrBlankOrEmptyOrTooShortOrTooLong_ThenPlainPasswordLengthException(String str){
        // Arrange
        String password = switch (str) {
            case "null" -> null;
            case "blank" -> "   ";
            case "empty" -> "";
            case "tooShort" -> "1".repeat(Cons.User.Validations.MIN_PASSWORD_LENGTH - 1);
            case "tooLong" -> "2".repeat(Cons.User.Validations.MAX_PASSWORD_LENGTH_PLAIN + 1);
            default -> throw new IllegalStateException("unexpected value: " + str);
        };

        CreateUserDTO dtoToCreate = createValidDTO();
        dtoToCreate.setPassword(password);

        // Act & Assert
        assertThatThrownBy(() -> userService.create(dtoToCreate, ERole.ROLE_USER))
                .isInstanceOf(PlainPasswordLengthException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Validations.PASSWORD_LENGTH_FAIL_MSG)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @Tag("create")
    void create_usernameAlreadyExists_ThenUsernameAlreadyExistException(){
        // Arrange
        CreateUserDTO dtoToCreate = createValidDTO();

        when(userRepository.existsByUsername(dtoToCreate.getUsername())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.create(dtoToCreate, ERole.ROLE_USER))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Constrains.USERNAME_UNIQUE_MSG)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
        verify(userRepository).existsByUsername(dtoToCreate.getUsername());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @Tag("create")
    void create_emailAlreadyExists_ThenEmailAlreadyExistException(){
        // Arrange
        CreateUserDTO dtoToCreate = createValidDTO();

        when(userRepository.existsByEmail(dtoToCreate.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.create(dtoToCreate, ERole.ROLE_USER))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Constrains.EMAIL_UNIQUE_MSG)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
        verify(userRepository).existsByEmail(dtoToCreate.getEmail());
        verify(userRepository, never()).saveAndFlush(any());
    }


    private UserEntity createUserEntityWithIdAndRolesWithId() {
        return UserEntity.builder()
                .id(1L)
                .username("cris6h16")
                .email("cristianmherrera21@gmail.com")
                .password("{bcrypt}$2a81...")
                .roles(new HashSet<>(Collections.singleton(RoleEntity.builder().id(1L).name(ERole.ROLE_USER).build())))
                .createdAt(new Date())
                .build();
    }


    /**
     * Create a {@link CreateUserDTO} with valid data.
     *
     * @return {@link CreateUserDTO}
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private CreateUserDTO createValidDTO() {
        return CreateUserDTO.builder()
                .username("cris6h16")
                .password("12345678")
                .email("cristianmherrera21@gmail.com")
                .build();
    }


    @Test
    @Tag("getById")
    void getById_UserFoundWithRoles_Successful() {
        // Arrange
        UserEntity entity = createUserEntityWithIdAndRolesWithId();
        Set<PublicRoleDTO> rolesOwned = entity.getRoles().stream()
                .map(role -> new PublicRoleDTO(role.getName()))
                .collect(Collectors.toSet());

        when(userRepository.findById(entity.getId())).thenReturn(Optional.of(entity));

        // Act
        PublicUserDTO dto = userService.getById(entity.getId());

        // Assert
        assertThat(dto)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", entity.getId())
                .hasFieldOrPropertyWithValue("username", entity.getUsername())
                .hasFieldOrPropertyWithValue("email", entity.getEmail())
                .hasFieldOrPropertyWithValue("createdAt", entity.getCreatedAt())
                .hasFieldOrPropertyWithValue("updatedAt", entity.getUpdatedAt())
                .hasFieldOrPropertyWithValue("roles", new HashSet<>(rolesOwned));
        verify(userRepository).findById(entity.getId());
    }

    @Tag("getById")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})/* -999 == null */
    void getById_IdNullOrLessThan1_ThenInvalidIdException(long longs) {
        // Arrange
        Long id = longs == -999 ? null : longs;

        // Act & Assert
        assertThatThrownBy(() -> userService.getById(id))
                .isInstanceOf(InvalidIdException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).findById(any());
    }

    @Test
    @Tag("getById")
    void getById_UserNotFound_ThenUserNotFoundException() {
        // Arrange
        Long id = 1L;
        Optional<UserEntity> entity = Optional.empty();

        when(userRepository.findById(id)).thenReturn(entity);

        // Act & Assert
        assertThatThrownBy(() -> userService.getById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);

        verify(userRepository).findById(id);
    }


    @Test
    @Tag("getById")
    void getById_UserFoundWithRolesNull_thenInRolesReturnEmptySet_Successful() {
        // Arrange
        UserEntity entity = createUserEntityWithIdAndRolesWithId();
        entity.setRoles(null);

        when(userRepository.findById(entity.getId())).thenReturn(Optional.of(entity));

        // Act
        PublicUserDTO dto = userService.getById(entity.getId());

        // Assert
        assertThat(dto)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", entity.getId())
                .hasFieldOrPropertyWithValue("username", entity.getUsername())
                .hasFieldOrPropertyWithValue("email", entity.getEmail())
                .hasFieldOrPropertyWithValue("createdAt", entity.getCreatedAt())
                .hasFieldOrPropertyWithValue("updatedAt", entity.getUpdatedAt())
                .hasFieldOrPropertyWithValue("roles", new HashSet<>(0));
        verify(userRepository).findById(entity.getId());
    }


    @Test
    @Tag("deleteById")
    void deleteById_Successful() {
        // Arrange
        Long id = 1L;

        given(userRepository.existsById(id)).willReturn(true);
        doNothing().when(userRepository).deleteById(id);

        // Act
        userService.deleteById(id);

        // Assert
        verify(userRepository).deleteById(id);
    }

    @Tag("deleteById")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})/* -999 == null */
    void deleteById_IdNullOrLessThan1_ThenInvalidIdException(long longs) {
        // Arrange
        Long id = longs == -999 ? null : longs;

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteById(id))
                .isInstanceOf(InvalidIdException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @Tag("deleteById")
    void deleteById_UserNotFound_ThenUserNotFoundException() {
        // Arrange
        Long id = 1L;
        when(userRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        verify(userRepository).existsById(id);
    }


    @Test
    @Tag("getPage")
    void getPage_ReturnList_Successful() {
        // Arrange
        int amount = 10;
        List<UserEntity> entities = getUserEntities(amount);
        Pageable pag = PageRequest.of(1, 5, Sort.by(Sort.Order.asc("id")));
        PageImpl<UserEntity> mockPage = new PageImpl<>(entities, pag, entities.size()); // 10
                when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(mockPage);

        // Act
        Page<PublicUserDTO> pageRes = userService.getPage(pag);

        // Assert
        assertEquals( pageRes.getTotalElements(), entities.size());
        assertEquals(pageRes.getTotalPages(), 2);
        assertEquals(pageRes.getNumber(), 1);
        assertEquals(pageRes.getSize(), 5);
        assertEquals(pageRes.isLast(), true );
        assertEquals(pageRes.isFirst(), false);
        assertEquals(pageRes.isEmpty(), false);

        for (int i = 0; i < entities.size(); i++) {
            assertThat(pageRes.getContent().get(i))
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("id", entities.get(i).getId())
                    .hasFieldOrPropertyWithValue("username", entities.get(i).getUsername())
                    .hasFieldOrPropertyWithValue("email", entities.get(i).getEmail())
                    .hasFieldOrPropertyWithValue("createdAt", entities.get(i).getCreatedAt())
                    .hasFieldOrPropertyWithValue("updatedAt", entities.get(i).getUpdatedAt())
                    .hasFieldOrPropertyWithValue("roles", new HashSet<>(Collections.singleton(new PublicRoleDTO(ERole.ROLE_USER))));
        }
        verify(userRepository).findAll(pag);
    }

    @Test
    @Tag("getPage")
    void getPage_nullPageable_ThenIllegalArgumentException() {
        // Arrange
        Pageable pag = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.getPage(pag))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userRepository, never()).findAll(any(Pageable.class));
    }


    @Test
    @Tag("patchUsernameById")
    void patchUsernameById_Successful() {
        // Arrange
        Long id = 1L;
        String newUsername = "newUsername";
        PatchUsernameUserDTO dto = new PatchUsernameUserDTO(newUsername);

        String cleanUsername = newUsername.trim().toLowerCase();

        when(userRepository.existsById(id)).thenReturn(true);
        when(userRepository.existsByUsername(cleanUsername)).thenReturn(false);
        doNothing().when(userRepository).updateUsernameById(cleanUsername, id);

        // Act
        userService.patchUsernameById(id, dto);

        // Assert
        verify(userRepository).existsById(id);
        verify(userRepository).existsByUsername(cleanUsername);
        verify(userRepository).updateUsernameById(cleanUsername, id);
    }

    @Tag("patchUsernameById")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})/* -999 == null */
    void patchUsernameById_IdNullOrLessThan1_ThenInvalidIdException(long longs) {
        // Arrange
        Long id = longs == -999 ? null : longs;
        PatchUsernameUserDTO dto = new PatchUsernameUserDTO("newUsername");

        // Act & Assert
        assertThatThrownBy(() -> userService.patchUsernameById(id, dto))
                .isInstanceOf(InvalidIdException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).updateUsernameById(any(), any());
    }

    @Test
    @Tag("patchUsernameById")
    void patchUsernameById_DTONull_ThenAnyUserDTOIsNullException() {
        // Arrange
        Long id = 1L;
        PatchUsernameUserDTO dto = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.patchUsernameById(id, dto))
                .isInstanceOf(AnyUserDTOIsNullException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.DTO.ANY_RELATED_DTO_WITH_USER_NULL);
        verify(userRepository, never()).updateUsernameById(any(), any());
    }


    @Test
    @Tag("patchUsernameById")
    void patchUsernameById_TrimFields() {
        // Arrange
        Long id = 1L;
        String newUsername = "  newUsername    ";
        PatchUsernameUserDTO dto = new PatchUsernameUserDTO(newUsername);

        String cleanUsername = newUsername.trim().toLowerCase();

        when(userRepository.existsById(id)).thenReturn(true);
        when(userRepository.existsByUsername(cleanUsername)).thenReturn(false);
        doNothing().when(userRepository).updateUsernameById(cleanUsername, id);

        // Act
        userService.patchUsernameById(id, dto);

        // Assert
        verify(userRepository).updateUsernameById(cleanUsername, id);
    }



    @Tag("patchUsernameById")
    @ParameterizedTest
    @ValueSource(strings = {"null", "blank", "empty", "tooShort", "tooLong"})
    void patchUsernameById_usernameNullOrBlankOrEmptyOrTooShortOrTooLong_ThenUsernameLengthException(String str) {
        // Arrange
        Long id = 1L;
        String username = switch (str) {
            case "null" -> null;
            case "blank" -> "   ";
            case "empty" -> "";
            case "tooShort" -> "a".repeat(Cons.User.Validations.MIN_USERNAME_LENGTH - 1);
            case "tooLong" -> "b".repeat(Cons.User.Validations.MAX_USERNAME_LENGTH + 1);
            default -> throw new IllegalStateException("unexpected value: " + str);
        };

        PatchUsernameUserDTO dto = new PatchUsernameUserDTO(username);

        // Act & Assert
        assertThatThrownBy(() -> userService.patchUsernameById(id, dto))
                .isInstanceOf(UsernameLengthException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Validations.USERNAME_LENGTH_FAIL_MSG)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).updateUsernameById(any(), any());
    }

    @Test
    @Tag("patchUsernameById")
    void patchUsernameById_UserNotFound_ThenUserNotFoundException() {
        // Arrange
        Long id = 1L;
        PatchUsernameUserDTO dto = new PatchUsernameUserDTO("newUsername");

        when(userRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.patchUsernameById(id, dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        verify(userRepository).existsById(id);
        verify(userRepository, never()).updateUsernameById(any(), any());
    }


    @Test
    @Tag("patchUsernameById")
    void patchUsernameById_UsernameAlreadyExists_ThenUsernameAlreadyExistsException() {
        // Arrange
        Long id = 1L;
        String newUsername = "newUsername";
        PatchUsernameUserDTO dto = new PatchUsernameUserDTO(newUsername);

        String cleanUsername = newUsername.trim().toLowerCase();

        when(userRepository.existsById(id)).thenReturn(true);
        when(userRepository.existsByUsername(cleanUsername)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.patchUsernameById(id, dto))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Constrains.USERNAME_UNIQUE_MSG)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
        verify(userRepository).existsById(id);
        verify(userRepository).existsByUsername(cleanUsername);
        verify(userRepository, never()).updateUsernameById(any(), any());
    }


    @Test
    @Tag("patchEmailById")
    void patchEmailById_Successful() {
        // Arrange
        Long id = 1L;
        String newEmail = "cristianmherrera21@gmail.com";
        PatchEmailUserDTO dto = new PatchEmailUserDTO(newEmail);

        when(userRepository.existsById(id)).thenReturn(true);
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        doNothing().when(userRepository).updateEmailById(newEmail, id);

        // Act
        userService.patchEmailById(id, dto);

        // Assert
        verify(userRepository).existsById(id);
        verify(userRepository).existsByEmail(newEmail);
        verify(userRepository).updateEmailById(newEmail, id);
    }

    @Tag("patchEmailById")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})/* -999 == null */
    void patchEmailById_IdNullOrLessThan1_ThenInvalidIdException(long longs) {
        // Arrange
        Long id = longs == -999 ? null : longs;
        PatchEmailUserDTO dto = new PatchEmailUserDTO("cristianmherrera21@gmail.com");

        // Act & Assert
        assertThatThrownBy(() -> userService.patchEmailById(id, dto))
                .isInstanceOf(InvalidIdException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).updateEmailById(any(), any());
    }

    @Test
    @Tag("patchEmailById")
    void patchEmailById_DTONull_ThenAnyUserDTOIsNullException() {
        // Arrange
        Long id = 1L;
        PatchEmailUserDTO dto = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.patchEmailById(id, dto))
                .isInstanceOf(AnyUserDTOIsNullException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.DTO.ANY_RELATED_DTO_WITH_USER_NULL);
        verify(userRepository, never()).updateEmailById(any(), any());
    }


    @Test
    @Tag("patchEmailById")
    void patchEmailById_TrimFields() {
        // Arrange
        Long id = 1L;
        String newEmail = "  cristianmherrera21@gmail.com    ";
        PatchEmailUserDTO dto = new PatchEmailUserDTO(newEmail);

        String cleanEmail = newEmail.trim().toLowerCase();

        when(userRepository.existsById(id)).thenReturn(true);
        when(userRepository.existsByEmail(cleanEmail)).thenReturn(false);
        doNothing().when(userRepository).updateEmailById(cleanEmail, id);

        // Act
        userService.patchEmailById(id, dto);

        // Assert
        verify(userRepository).updateEmailById(cleanEmail, id);
    }

    @Tag("patchEmailById")
    @ParameterizedTest
    @ValueSource(strings = {"null", "blank", "empty", "tooShort", "tooLong", "isNotEmail"})
    void patchEmailById_emailNullOrBlankOrEmptyOrTooShortOrTooLongOrIsNotEmail_ThenEmailIsInvalidException(String str) {
        // Arrange
        Long id = 1L;
        String email = switch (str) {
            case "null" -> null;
            case "blank" -> "   ";
            case "empty" -> "";
            case "tooShort" -> "a".repeat(Cons.User.Validations.MIN_EMAIL_LENGTH - 1);
            case "tooLong" -> "b".repeat(Cons.User.Validations.MAX_EMAIL_LENGTH + 1);
            case "isNotEmail" -> "thisIsNotAnEmail";
            default -> throw new IllegalStateException("unexpected value: " + str);
        };

        PatchEmailUserDTO dto = new PatchEmailUserDTO(email);

        // Act & Assert
        assertThatThrownBy(() -> userService.patchEmailById(id, dto))
                .isInstanceOf(EmailIsInvalidException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Validations.EMAIL_IS_INVALID_MSG)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).updateEmailById(any(), any());
    }

    @Test
    @Tag("patchEmailById")
    void patchEmailById_UserNotFound_ThenUserNotFoundException() {
        // Arrange
        Long id = 1L;
        PatchEmailUserDTO dto = new PatchEmailUserDTO("cristianmherrera21@gmail.com");

        when(userRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.patchEmailById(id, dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        verify(userRepository, never()).updateEmailById(any(), any());
    }

    @Test
    @Tag("patchEmailById")
    void patchEmailById_EmailAlreadyExists_ThenEmailAlreadyExistException() {
        // Arrange
        Long id = 1L;
        String newEmail = "cristianmherrera21@gmail.com";
        PatchEmailUserDTO dto = new PatchEmailUserDTO(newEmail);

        when(userRepository.existsById(id)).thenReturn(true);
        when(userRepository.existsByEmail(newEmail)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.patchEmailById(id, dto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Constrains.EMAIL_UNIQUE_MSG)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
        verify(userRepository, never()).updateEmailById(any(), any());
    }


    @Test
    @Tag("patchPasswordById")
    void patchPasswordById_Successful() {
        // Arrange
        Long id = 1L;
        String newPassword = "12345678";
        PatchPasswordUserDTO dto = new PatchPasswordUserDTO(newPassword);

        when(userRepository.existsById(id)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("{bcrypt}$2a81...");
        doNothing().when(userRepository).updatePasswordById("{bcrypt}$2a81...", id);

        // Act
        userService.patchPasswordById(id, dto);

        // Assert
        verify(userRepository).existsById(id);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).updatePasswordById("{bcrypt}$2a81...", id);
    }

    @Tag("patchPasswordById")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})/* -999 == null */
    void patchPasswordById_IdNullOrLessThan1_ThenInvalidIdException(long longs) {
        // Arrange
        Long id = longs == -999 ? null : longs;
        PatchPasswordUserDTO dto = new PatchPasswordUserDTO("12345678");

        // Act & Assert
        assertThatThrownBy(() -> userService.patchPasswordById(id, dto))
                .isInstanceOf(InvalidIdException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.CommonInEntity.ID_INVALID)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).updatePasswordById(any(), any());
    }

    @Test
    @Tag("patchPasswordById")
    void patchPasswordById_DTONull_ThenAnyUserDTOIsNullException() {
        // Arrange
        Long id = 1L;
        PatchPasswordUserDTO dto = null;

        // Act & Assert
        assertThatThrownBy(() -> userService.patchPasswordById(id, dto))
                .isInstanceOf(AnyUserDTOIsNullException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.DTO.ANY_RELATED_DTO_WITH_USER_NULL);
        verify(userRepository, never()).updatePasswordById(any(), any());
    }

    @Test
    @Tag("patchPasswordById")
    void patchPasswordById_TrimFields() {
        // Arrange
        Long id = 1L;
        String newPassword = "  12345678H   ";
        PatchPasswordUserDTO dto = new PatchPasswordUserDTO(newPassword);

        String cleanPassword = newPassword.trim();

        when(userRepository.existsById(id)).thenReturn(true);
        when(passwordEncoder.encode(cleanPassword)).thenReturn("{bcrypt}$2a81...");
        doNothing().when(userRepository).updatePasswordById("{bcrypt}$2a81...", id);

        // Act
        userService.patchPasswordById(id, dto);

        // Assert
        verify(passwordEncoder).encode(cleanPassword);
        verify(userRepository).updatePasswordById("{bcrypt}$2a81...", id);
    }


    @Tag("patchPasswordById")
    @ParameterizedTest
    @ValueSource(strings = {"null", "blank", "empty", "tooShort", "tooLong"})
    void patchPasswordById_passwordNullOrBlankOrEmptyOrTooShortOrTooLong_ThenPlainPasswordLengthException(String str) {
        // Arrange
        Long id = 1L;
        String password = switch (str) {
            case "null" -> null;
            case "blank" -> "   ";
            case "empty" -> "";
            case "tooShort" -> "1".repeat(Cons.User.Validations.MIN_PASSWORD_LENGTH - 1);
            case "tooLong" -> "2".repeat(Cons.User.Validations.MAX_PASSWORD_LENGTH_PLAIN + 1);
            default -> throw new IllegalStateException("unexpected value: " + str);
        };

        PatchPasswordUserDTO dto = new PatchPasswordUserDTO(password);

        // Act & Assert
        assertThatThrownBy(() -> userService.patchPasswordById(id, dto))
                .isInstanceOf(PlainPasswordLengthException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Validations.PASSWORD_LENGTH_FAIL_MSG)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).updatePasswordById(any(), any());
    }


    @Test
    @Tag("patchPasswordById")
    void patchPasswordById_UserNotFound_ThenUserNotFoundException() {
        // Arrange
        Long id = 1L;
        PatchPasswordUserDTO dto = new PatchPasswordUserDTO("12345678");

        when(userRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.patchPasswordById(id, dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasFieldOrPropertyWithValue("reason", Cons.User.Fails.NOT_FOUND)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        verify(userRepository, never()).updatePasswordById(any(), any());
    }


    @Test
    @Tag("deleteAll")
    void deleteAll_Successful() {
        doNothing().when(userRepository).deleteAll();
        userService.deleteAll();
        verify(userRepository).deleteAll();
    }


    /**
     * Create a list of {@link UserEntity} with roles, for simulate
     * a page of persisted users with their roles.
     *
     * @param amount amount of users to create
     * @return list of {@link UserEntity} with roles
     * @author <a href="https://www.github.com/cris6h16" target="_blank">Cristian Herrera</a>
     * @since 1.0
     */
    private List<UserEntity> getUserEntities(int amount) {
        List<UserEntity> entities = new ArrayList<>();
        for (long i = 0; i < amount; i++) {
            entities.add(UserEntity.builder()
                    .id(i)
                    .username("cris6h16" + i)
                    .email(i + "cristianmherrera21@gmail.com")
                    .password("{bcrypt}$2a81..." + i)
                    .roles(new HashSet<>(Collections.singleton(RoleEntity.builder().id(i).name(ERole.ROLE_USER).build())))
                    .createdAt(new Date())
                    .build());
        }
        return entities;

    }
}
