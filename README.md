# api-rest-spring-boot-basic-auth-TDD--notes-app

Implementing a Semantic API REST in Spring Boot, using basic auth applying TDD, this is a little Note Application which
will use PostgresSQL (password is used encrypted for everything).

## 1. DEFINING THE PROJECT

### 1.1. User Case

- ANONYMOUS CAN:
    - create an account


- USER CAN:
    - update his account data
    - create a note
    - update a note
    - "delete" a note
    - list all his notes
    - See a note
    - delete his account


- ADMIN CAN:
    - list all users

### 1.2. Some considerations

- If the user isn't the owner of the note, then for him the note doesn't exist.
- The user can't hard delete a note or his account, only soft delete.
- The user can't modify audit fields.
- remember that `PUT` update or create.
- user can only get his profile information
- we'll return body in something goes wrong for those which isn't necessary return a response body if was successful

### 1.3 Endpoints

| URI                                                                            | HTTP METHOD | SUCCESS          | DESC             | Authority    |
|--------------------------------------------------------------------------------|-------------|------------------|------------------|--------------|
| `/api/users/{id}`                                                              | `GET`       | `200 OK`         | _see a user_     | `ROLE_USER`  |
| `/api/users`                                                                   | `POST`      | `201 CREATED`    | _create a user_  |              |
| `/api/users`                                                                   | `PATCH`     | `204 NO CONTENT` | _update a user_  | `ROLE_USER`  |         
| `/api/users`                                                                   | `DELETE`    | `204 NO CONTENT` | _delete a user_  | `ROLE_USER`  |
| `/api/notes`                                                                   | `POST`      | `201 CREATED`    | _create a note_  | `ROLE_USER`  |
| `/api/notes`<br/>- `page=<1>`<br/>- `size=<2>`<br/>- `sort=<create_at>, <asc>` | `GET`       | `200 OK`         | _list all notes_ | `ROLE_USER`  |
| `/api/notes/{id}`                                                              | `GET`       | `200 OK`         | _see a note_     | `ROLE_USER`  |
| `/api/notes/{id}`                                                              | `PUT`       | `204 NO CONTENT` | _update a note_  | `ROLE_USER`  |
| `/api/notes/{id}`                                                              | `DELETE`    | `204 NO CONTENT` | _delete a note_  | `ROLE_USER`  |
| `/api/users`<br/>- `page=<1>`<br/>- `size=<2>`<br/>- `sort=<create_at>, <asc>` | `GET`       | `200 OK`         | _list all users_ | `ROLE_ADMIN` |

### 1.4 Entities

- User
    - id
    - username (max=20 & min=1)
    - email
    - password (min=8)
    - created_at
    - updated_at
    - deleted_at
    - roles
    - notes


- Note
    - id
    - title (MAx=255)
    - content
    - ~~created_at~~ (I won't add it for use `PUT`)
    - updated_at
    - deleted_at


- Role
    - id
    - name (ERole)

## 2 API REST

### 2.1 CREATE a USER

| URI          | HTTP METHOD | SUCCESS       |
|--------------|-------------|---------------|
| `/api/users` | `POST`      | `201 CREATED` | 

| FAIL                                                                                                                                                    | RESPONSE          |
|---------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------|
| - _Username already exists_<br/>- _Email already exists_                                                                                                | `409 Conflict`    |
| - _Password must be at least 8 characters_<br/>- _Email is invalid_<br/>- _Email, Username and Password are required_<br/>- _Username mustn't be blank_ | `400 Bad Request` |

### 2.2 UPDATE a USER

| URI          | HTTP METHOD | SUCCESS          |
|--------------|-------------|------------------|
| `/api/users` | `PATCH`     | `204 NO CONTENT` |

| FAIL                                                                          | RESPONSE            |
|-------------------------------------------------------------------------------|---------------------|
| - _Username already exists_<br/>- _Email already exists_                      | `409 Conflict`      |
| - _You need to be authenticated to perform this action_                       | `401 Unauthorized`  |
| - _You aren't the owner of this id_ ( whether this Id exists/doesn't exist  ) | `403 Forbidden`     |
| - _Password must be at least 8 characters_<br/>- _Email is invalid_           | `400 Bad Request`   |
| ~~- _User not found_~~ -> _"replace" by -_ _You aren't the owner of this id_  | ~~`404 Not Found`~~ |

### 2.3 GET a USER

| URI               | HTTP METHOD | SUCCESS  |
|-------------------|-------------|----------|
| `/api/users/{id}` | `GET`       | `200 OK` |

| FAIL                                                                            | RESPONSE            |
|---------------------------------------------------------------------------------|---------------------|
| - _You need to be authenticated to perform this action_                         | `401 Unauthorized`  |
| - _You aren't the owner of this ID_   ( whether this Id exists/doesn't exist  ) | `403 Forbidden`     |
| ~~- _User not found_~~                                                          | ~~`404 Not Found`~~ |

<hr>  

### 2.3 CREATE a NOTE

| URI          | HTTP METHOD | SUCCESS       |
|--------------|-------------|---------------|
| `/api/notes` | `POST`      | `201 CREATED` |

| FAIL                                                                                                    | RESPONSE           |
|---------------------------------------------------------------------------------------------------------|--------------------|
| - _Title is required \*\*\*if only spaces or null\*\*\*_<br/>- _Title must be less than 255 characters_ | `400 Bad Request`  |
| - _You need to be authenticated to perform this action_                                                 | `401 Unauthorized` |

### 2.4 LIST all NOTES

| URI                                                                            | HTTP METHOD | SUCCESS  |
|--------------------------------------------------------------------------------|-------------|----------|
| `/api/notes`<br/>- `page=<1>`<br/>- `size=<2>`<br/>- `sort=<create_at>, <asc>` | `GET`       | `200 OK` |

| FAIL                                                    | RESPONSE           |
|---------------------------------------------------------|--------------------|
| - _You need to be authenticated to perform this action_ | `401 Unauthorized` |

### 2.5 SEE a NOTE

| URI               | HTTP METHOD | SUCCESS  |
|-------------------|-------------|----------|
| `/api/notes/{id}` | `GET`       | `200 OK` |

| FAIL                                                    | RESPONSE           |
|---------------------------------------------------------|--------------------|
| - _You need to be authenticated to perform this action_ | `401 Unauthorized` |
| - _Note not found_                                      | `404 Not Found`    |

### 2.6 UPDATE/CREATE a NOTE (pass the id)

| URI              | HTTP METHOD | SUCCESS          |
|------------------|-------------|------------------|
| `api/notes/{id}` | `PUT`       | `204 NO CONTENT` |

| FAIL                                                                     | RESPONSE           |
|--------------------------------------------------------------------------|--------------------|
| - _Title is required_<br/>- _Title must be between 1 and 255 characters_ | `400 Bad Request`  |`
| - _You need to be authenticated to perform this action_                  | `401 Unauthorized` |

### 2.7 DELETE a NOTE

| URI              | HTTP METHOD | SUCCESS          |
|------------------|-------------|------------------|
| `api/notes/{id}` | `DELETE`    | `204 NO CONTENT` |

| FAIL                                                    | RESPONSE           |
|---------------------------------------------------------|--------------------|
| - _You need to be authenticated to perform this action_ | `401 Unauthorized` |
| - _Note not found_                                      | `404 Not Found`    |

<hr>

### 2.8 LIST all USERS

| URI                                                                            | HTTP METHOD | SUCCESS  |
|--------------------------------------------------------------------------------|-------------|----------|
| `/api/users`<br/>- `page=<1>`<br/>- `size=<2>`<br/>- `sort=<create_at>, <asc>` | `GET`       | `200 OK` |

| FAIL                                                    | RESPONSE           |
|---------------------------------------------------------|--------------------|
| - _You need to be authenticated to perform this action_ | `401 Unauthorized` |
| - _You need to be an admin to perform this action_      | `403 Forbidden`    |

#

## Time taken (UTC-5)

**PD**: This isn't classified very well, but I'll try to do it better in the future.   
also take into account that I'm already have an idea of what the api will do. Only was capture my general idea in
specific points (for defining & things like that).

### README.md

1. Setup project

| Date       | Defined | Start | End   |
|------------|---------|-------|-------|
| 2024-04-28 |         | 11:50 | 12:21 |

2. [DEFINING THE PROJECT](#1-defining-the-project)

| Date       | Defined                                                       | Start | End   |
|------------|---------------------------------------------------------------|-------|-------|
| 2024-04-28 | [User Case & Endpoints](#11-user-case)                        | 12:47 | 13:12 |
| 2024-04-28 | [Entities](#14-entities) & Corrections(endpoints, error code) | 13:30 | 14:00 |

3. [API REST](#2-API-REST)

| Date       | Defined                                                                             | Start           | End             |
|------------|-------------------------------------------------------------------------------------|-----------------|-----------------|
| 2024-04-28 | [USER CREATION](#21-create-a-user)                                                  | 12:24           | 12:47           |
| 2024-04-28 | [UPDATED A USER](#22-update-a-user)                                                 | 13:15           | 13:30           |
| 2024-04-28 | [OPERATIONS WITH NOTES](#23-create-a-note) & Corrections & last review of responses | 14:00<br/>17:22 | 14:30<br/>17:50 |
| 2024-04-28 | git conflict                                                                        | 14:30           | 14:41           |
| 2024-04-28 | Review & Corrections in Response Codes                                              | 15:50           | 16:05           |

**∞**. Additional

| Date       | Defined              | Start | End   |
|------------|----------------------|-------|-------|
| 2024-04-28 | ENHANCED readability | 21:35 | 21:45 |

### IMplementation

4. Beginning the Implementation

| Date                                     | Defined                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | Start                                                               | End                              |
|------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|----------------------------------|
| 2024-04-28                               | [shouldCreateAUser()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | 21:55                                                               | 22:44                            |
| 2024-04-28                               | Correct(IMPL) datasource, Impl Entities, DTO, .YAML, etc<br/>basically the second startup                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        | 22:44                                                               | 00:15                            |
| 2024-04-28                               | Analyzing a error that I was([@JsonFormat](src/main/java/org/cris6h16/apirestspringboot/DTOs/CreateUserDTO.java))                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                | 00:15                                                               | 00:30                            |
| 2024-04-29<br/>2024-04-30                | Implementing(Repository, Entities, Security, etc)    --> [shouldCreateAUser()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>Test passed                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 | 19:13<br/>14:15                                                     | 20:33<br/>15:09                  |
| 2024-04-30                               | [shouldNotCreateAUser_usernameAlreadyExists()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java) <br/> & Test passed                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | 15:40                                                               | 16:10                            |
| 2024-04-30                               | [shouldNotCreateAUser_EmailAlreadyExists()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java) <br/> & Test passed                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                | 16:15                                                               | 16:35                            |
| 2024-04-30                               | [shouldNotCreateAUser_PasswordTooShort()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java) <br/> & Test passed <br/> & Analyzing why always is greater that 8 <br/>(is saved encrypted we need verify length before)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | 16:40                                                               | 16:57                            |
| 2024-04-30                               | [shouldNotCreateAUser_EmailIsInvalid()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java) <br/> & Test passed                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    | 17:00                                                               | 17:10                            |
| 2024-05-01                               | [shouldNotCreateAUser_UsernameIsNullOrEmpty()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldNotCreateAUser_PasswordTooShort()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldNotCreateAUser_PasswordIsNullOrEmpty](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldNotCreateAUser_EmailIsNullOrEmpty](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/> Investigating & analyzing why some data constraints aren't  respected<br/> and find a way to handle better the exceptions (response body) <br/>Correct all responses (standardize the errors responses)<br/> [see that](#things-that-caused-me-troubles)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                | 16:55                                                               | 20:10                            | 
| 2024-05-03                               | Add/Improve docs/comments                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        | 17:25                                                               | 17:48                            |
|                                          |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |                                                                     |                                  |
| 2024-05-03<br/>2024-05-04                | Tests added(UPDATE USER): <br/>[shouldUpdateUsername()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldUpdateEmail()]((src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java))<br/>[shouldUpdatePassword()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldUpdateUsernameEmailPassword()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldBeGreaterUpdatedAt()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[updateShouldNotChange_DeletedAtCreatedAtRolesNotes()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldNotUpdateUsernameAlreadyExists()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldNotUpdateEmailAlreadyExists()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldNotUpdateYouNeedToBeAuthenticated()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldNotUpdateYouCannotUpdateOtherUserAccount()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldNotUpdateUserNotFound()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldNotUpdateEmailIsInvalid()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldNotUpdatePasswordTooShort()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java) | 18:06<br/> 02:20                                                    | 20:30<br/> 03:20                 |
| 2024-05-04                               | Implementing the above test && Tests passed                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      | 10:05<br/>16:22                                                     | 12:20<br/>18:17                  |
| 2024-05-04<br/><br/><br/><br/>2024-05-05 | Tests added(GET USER):<br/>[shouldGetAUserById()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldNotGetIsNotAuthenticated()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>[shouldNotGetUserYouAreNotTheOwner()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)_<br/> Implementing & Passing the tests                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | 21:32<br/><br/><br/><br/>07:51                                      | 21:54<br/><br/><br/><br/>8:17    |
| 2024-05-05 <br/><br/>                    | corrected security issues in [MyAuthorizationService](src/main/java/org/cris6h16/apirestspringboot/Config/Service/CustomAuthHandler/MyAuthorizationService.java) <br/>Adding  Test(CREATE A NOTE):<br/> [shouldCreateANote()](src/test/java/org/cris6h16/apirestspringboot/Controllers/NoteControllerTest.java)<br/>[shouldNotCreateANoteTitleIsNull()](src/test/java/org/cris6h16/apirestspringboot/Controllers/NoteControllerTest.java)<br/>[shouldNotCreateANoteTitleIsBlank()](src/test/java/org/cris6h16/apirestspringboot/Controllers/NoteControllerTest.java)<br/>[shouldNotCreateANoteTitleLengthIsGreaterThan255()](src/test/java/org/cris6h16/apirestspringboot/Controllers/NoteControllerTest.java)<br/>[shouldNotCreateANoteMustBeAuthenticated()](src/test/java/org/cris6h16/apirestspringboot/Controllers/NoteControllerTest.java)<br/>Corrected the entities relationship<br/>added [NoteEntities.txt](src/test/resources/NotesEntities.txt) -> are serialized in JSON -> for the next tests<br/>Tests turn to green                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              | 8:20<br/>11:37<br>13:42<br/>+ aprox: 1 hour I forget take last time | 10:20<br/>12:05<br>14:40<br><br> |
| 2024-05-05                               | GetNotes Tests(Pageable):<br/>[shouldListAllNotesIsPageable()](src/test/java/org/cris6h16/apirestspringboot/Controllers/NoteControllerTest.java)<br>[~~shouldNotListAllNotesIsPageable()~~](src/test/java/org/cris6h16/apirestspringboot/Controllers/NoteControllerTest.java)<br/>[shouldNotListAllNotesIsNotAuthenticated()](src/test/java/org/cris6h16/apirestspringboot/Controllers/NoteControllerTest.java)<br/>[URIWithNoParamsShouldListInDefaultPageableConfiguration()](src/test/java/org/cris6h16/apirestspringboot/Controllers/NoteControllerTest.java)<br>refactor [shouldListAllNotesIsPageable_ASC_DESC](src/test/java/org/cris6h16/apirestspringboot/Controllers/NoteControllerTest.java)<br/>&& Tests passed                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      | 19:25<br/>14:46<br/>18:00                                           | 20:06<br/>16:58<br/>18:35        |

### Some Questions that you probably have

1. How is taken the time ?

> The time is taken since I start to do it & when I finish it. NOT is only the time of coding or something like that.

2. Why are there very short/large cuts(time) between them ?

> I'm a student, I have to do other things(Homework, study for lessons, etc), and I'm a son, my parents unexpectedly
> call me to do something or else they get angry.   
> also I'm a human, I need to eat, sleep, etc...

## THINGS THAT CAUSED ME TROUBLES

1. Validations from different packages

> if you validate with a field like `@Length(min = .., max = .., message = ..)` which come
> from `org.hibernate.validator.constraints` when you want to **Validate** from a parameter for example (`@Valid`) which
> come from `<javax>.validation.constraints` you validation will be ignored(If you are validating an element that you
> won't persist) specially if you passed a null/empty value.
> if
> you want to validate the said use
`@Size` & `@NotBlank` (in `@Size` null elements are valid) from `<javax>.validation.constraints`; or directly
> use `@NotBlank`.

2. Standardize the responses

> One of the things that take additional time was that literally I had to guess the response body(this problem only if
> there was an exception)   
> I had been having 2 format of responses(and probably can easily be added more);
> 1. when I throw an `ResponseStatusException` or any of its subclasses from `@Service` for example.
> 2. when I handle exceptions from `@<RestController>Advice` which is different from the above.
> 3. possible increase the number of formats(if in each Exception handled in Advice I return a different format (very
     possible)).

> Did you see the problem? my way to solve it was make a common Exception response for
> all (`DataIntegrityViolationException`, `ConstraintViolationException`, `ResponseStatusException`, any threw in
> anywhere ) using a `Map` and `ObjectMapper` in `@<RestController>Advice`

// TODO: write about the definition of formats for exceptions/"errors", handling in HTTP handled classes exceptions, and
when throw a
ResponseStatusException, and when return a custom response entity




<hr>
//TODO: write about how all can test the API (POSTGRESQL setup)