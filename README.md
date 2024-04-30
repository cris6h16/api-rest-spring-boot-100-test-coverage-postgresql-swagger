# api-rest-spring-boot-basic-auth-TDD--notes-app

Implementing a Semantic API REST in Spring Boot, using basic auth applying TDD, this is a little Note Application which
will use PostgresSQL.

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

| FAIL                                                                                                                                                                      | RESPONSE          |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------|
| - _Username already exists_<br/>- _Email already exists_                                                                                                                  | `409 Conflict`    |
| - _Password must be at least 8 characters_<br/>- _Email is invalid_<br/>- _Email, Username and Password is required_<br/>- _Username must be between 1 and 20 characters_ | `400 Bad Request` |

### 2.2 UPDATE a USER

| URI          | HTTP METHOD | SUCCESS          |
|--------------|-------------|------------------|
| `/api/users` | `PATCH`     | `204 NO CONTENT` |

| FAIL                                                                                                                     | RESPONSE           |
|--------------------------------------------------------------------------------------------------------------------------|--------------------|
| - _Username already exists_<br/>- _Email already exists_                                                                 | `409 Conflict`     |
| - _You need to be authenticated to perform this action_<br/>- _You cannot updated other users Accounts_                  | `401 Unauthorized` |
| - _Password must be at least 8 characters_<br/>- _Email is invalid_<br/>- _Username must be between 1 and 20 characters_ | `400 Bad Request`  |

### 2.3 GET a USER

| URI               | HTTP METHOD | SUCCESS  |
|-------------------|-------------|----------|
| `/api/users/{id}` | `GET`       | `200 OK` |

| FAIL                                                    | RESPONSE           |
|---------------------------------------------------------|--------------------|
| - _You need to be authenticated to perform this action_ | `401 Unauthorized` |
| - _User not found_                                      | `404 Not Found`    |

<hr>  

### 2.3 CREATE a NOTE

| URI          | HTTP METHOD | SUCCESS       |
|--------------|-------------|---------------|
| `/api/notes` | `POST`      | `201 CREATED` |

| FAIL                                                                     | RESPONSE           |
|--------------------------------------------------------------------------|--------------------|
| - _Title is required_<br/>- _Title must be between 1 and 255 characters_ | `400 Bad Request`  |
| - _You need to be authenticated to perform this action_                  | `401 Unauthorized` |

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

| Date                      | Defined                                                                                                                                                                          | Start           | End             |
|---------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|-----------------|
| 2024-04-28                | UserController Test --> [shouldCreateAUser()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)                                                  | 21:55           | 22:44           |
| 2024-04-28                | Correct(IMPL) datasource, Impl Entities, DTO, .YAML, etc<br/>basically the second startup                                                                                        | 22:44           | 00:15           |
| 2024-04-28                | Analyzing a error that I was([@JsonFormat](src/main/java/org/cris6h16/apirestspringboot/DTOs/CreateUserDTO.java))                                                                | 00:15           | 00:30           |
| 2024-04-29<br/>2024-04-30 | Implementing(Repository, Entities, Security, etc)    --> [shouldCreateAUser()](src/test/java/org/cris6h16/apirestspringboot/Controllers/UserControllerTest.java)<br/>Test passed | 19:13<br/>14:15 | 20:33<br/>15:09 |

### Some Questions that you probably have

1. How is taken the time ?

> The time is taken since I start to do it & when I finish it. NOT is only the time of coding or something like that.

2. Why are there very short/large cuts(time) between them ?
> I'm a student, I have to do other things(Homework, study for lessons, etc),  and I'm a son, my parents unexpectedly call me to do something else they get angry.   
 also I'm a human, I need to eat, sleep, etc...
