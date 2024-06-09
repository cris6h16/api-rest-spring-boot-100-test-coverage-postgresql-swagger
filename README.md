# api-rest-spring-boot-basic-auth--notes-app

Implementing a Semantic API REST in Spring Boot, using basic auth applying ~~TDD~~, this is a little Note Application which
will use PostgresSQL (password is used encrypted for everything (store, runtime, etc)).

# TEST THIS API

1. Create a local DB in PostgresSQL:
    - `port`: `5432`
    - `database`: `api-rest-spring-boot`
    - `schema`: `test`

2. Environment variables:
    - `PSQL_PASS`: password of the user which has the grants for the created database
    - `PSQL_USER`: username of the user which has the grants for the created database

3. Verify the above:
    - `echo $PSQL_USER && echo $PSQL_PASS`: if you see the credentials, then you can continue
    - `psql -h localhost -p 5432 -U $PSQL_USER -d api-rest-spring-boot -c "SELECT schema_name FROM information_schema.schemata;"`:
      if you can see the schema `test`, then you can continue.

4. JDK:
    - `21`


- You can see the used dependencies in the `pom.xml` file    


#

[//]: # (todo: improve docs, add some diagrams, etc; but this as final step)

## 1. DEFINING THE PROJECT

### 1.1. User Case

- ANONYMOUS CAN:
    - create an account


- USER CAN:
    - update his account data
    - create a note
    - update a note
    - delete a note
    - list all his notes
    - See a note
    - delete his account


- ADMIN CAN:
    - list all users

### 1.2. Some considerations

- should Delete all notes when a user is deleted
- delete a note should not delete the user
- If the user isn't the owner of the note, then for him the note doesn't exist.
- The user can't modify audit fields.
- user can only get his profile information
- we'll return a body if something goes wrong.

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
    - ~~deleted_at~~ (User won't delete softly )
    - roles
    - notes


- Note
    - id
    - title (MAx=255)
    - content
    - ~~created_at~~ (I won't add it for use `PUT`)
    - updated_at
    - ~~deleted_at~~ (Note won't delete softly )


- Role
    - id
    - name (ERole)

## 2 API REST

### 2.1 CREATE a USER

| URI          | HTTP METHOD | SUCCESS       |
|--------------|-------------|---------------|
| `/api/users` | `POST`      | `201 CREATED` | 

| Action / Passing a [CreatedUserDTO](src/main/java/org/cris6h16/apirestspringboot/DTOs/CreateUserDTO.java) with a: | RESPONSE                                                                                                            | STATUS CODE       |
|-------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|-------------------|
| - `username` that already exists                                                                                  | - `Username already exists`                                                                                         | `409 Conflict`    |  
| - `email` that already exists                                                                                     | - `Email already exists`                                                                                            |                   |
|                                                                                                                   |                                                                                                                     |                   |
| - `password` length less than 8                                                                                   | - `Password must be at least 8 characters`                                                                          | `400 Bad Request` |    
| - `username` length greater than `20`                                                                             | - `Username must be less than 20 characters`                                                                        |                   | 
| - `email` that isn't valid                                                                                        | - `Email is invalid`                                                                                                |                   | 
| - `email` that is null                                                                                            | - `Email is required`                                                                                               |                   | 
| - `email` that has just white spaces                                                                              | - `Email is required`                                                                                               |                   | 
| - `username` that is null                                                                                         | - `Username mustn't be blank`                                                                                       |                   | 
| - `username` that has just white spaces                                                                           | - `Username mustn't be blank`                                                                                       |                   | 
| - `password` that is null                                                                                         | - `Password mustn't be blank`                                                                                       |                   | 
|                                                                                                                   |                                                                                                                     |                   |
| - ANY other exception(unhandled)                                                                                  | _any of defaults([Cons.ExceptionHandler.defMsg](src/main/java/org/cris6h16/apirestspringboot/Constants/Cons.java))_ | `400 Bad Request` |          

[center of response messages](src/main/java/org/cris6h16/apirestspringboot/Constants/Cons.java)

### 2.2 UPDATE a USER

| URI          | HTTP METHOD | SUCCESS          |
|--------------|-------------|------------------|
| `/api/users` | `PATCH`     | `204 NO CONTENT` |

| Action / PASSING a [UpdateUserDTO](src/main/java/org/cris6h16/apirestspringboot/DTOs/UpdateUserDTO.java) with a: | RESPONSE                                                                                                            | STATUS CODE        |
|------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|--------------------|
| - `username` that already exists                                                                                 | - `Username already exists`                                                                                         | `409 Conflict`     |
| - `email` that already exists                                                                                    | - `Email already exists`                                                                                            |                    |
| - `email` is invalid                                                                                             | - `Email is invalid`                                                                                                | `400 Bad Request`  |
| - `password` length less than 8                                                                                  | - `Password must be at least 8 characters`                                                                          |                    |
| - Isn't authenticated                                                                                            | _empty_                                                                                                             | `401 Unauthorized` |
| - Try to update other user account(doesnt matter if that user acc. exists or not)                                | - `You aren't the owner of this id`                                                                                 | `403 Forbidden`    |
|                                                                                                                  |                                                                                                                     |                    |
| - ANY other exception(unhandled)                                                                                 | _any of defaults([Cons.ExceptionHandler.defMsg](src/main/java/org/cris6h16/apirestspringboot/Constants/Cons.java))_ | `400 Bad Request`  |                  

### 2.3 GET a USER

| URI               | HTTP METHOD | SUCCESS  |
|-------------------|-------------|----------|
| `/api/users/{id}` | `GET`       | `200 OK` |

| Action                                           | RESPONSE                                                                                                            | STATUS CODE        |
|--------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|--------------------|
| - Isn't authenticated                            |                                                                                                                     | `401 Unauthorized` |
| - Try to get other user's account ( `!= /{id}` ) |                                                                                                                     | `403 Forbidden`    |
|                                                  |                                                                                                                     |                    |
| - ANY other exception(unhandled)                 | _any of defaults([Cons.ExceptionHandler.defMsg](src/main/java/org/cris6h16/apirestspringboot/Constants/Cons.java))_ | `400 Bad Request`  |           

<hr>  

### 2.3 CREATE a NOTE

| URI          | HTTP METHOD | SUCCESS       |
|--------------|-------------|---------------|
| `/api/notes` | `POST`      | `201 CREATED` |

| ACTION / PASSING a [CreateNoteDTO](src/main/java/org/cris6h16/apirestspringboot/DTOs/CreateNoteDTO.java) with a: | RESPONSE                                                                                                            | STATUS CODE        |
|------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|--------------------|
| - `title` that is null                                                                                           | - `Title is required`                                                                                               | `400 Bad Request`  |
| - `title` that has just white spaces                                                                             | - `Title is required`                                                                                               |                    |
| - `title` that is greater than 255 characters                                                                    | - `Title must be less than 255 characters`                                                                          |                    |
| - Is not authenticated                                                                                           | _void_                                                                                                              | `401 Unauthorized` |
|                                                                                                                  |                                                                                                                     |                    |
| - ANY other exception(unhandled)                                                                                 | _any of defaults([Cons.ExceptionHandler.defMsg](src/main/java/org/cris6h16/apirestspringboot/Constants/Cons.java))_ | `400 Bad Request`  |         

### 2.4 LIST all my NOTES

| URI                                                                            | HTTP METHOD | SUCCESS  |
|--------------------------------------------------------------------------------|-------------|----------|
| `/api/notes`<br/>- `page=<1>`<br/>- `size=<2>`<br/>- `sort=<create_at>, <asc>` | `GET`       | `200 OK` |

| ACTION                           | RESPONSE                                                                                                            | STATUS CODE        |
|----------------------------------|---------------------------------------------------------------------------------------------------------------------|--------------------|
| - _Is not authenticated_         | _void_                                                                                                              | `401 Unauthorized` |
|                                  |                                                                                                                     |                    |
| - ANY other exception(unhandled) | _any of defaults([Cons.ExceptionHandler.defMsg](src/main/java/org/cris6h16/apirestspringboot/Constants/Cons.java))_ | `400 Bad Request`  |               

### 2.5 SEE a NOTE

| URI               | HTTP METHOD | SUCCESS  |
|-------------------|-------------|----------|
| `/api/notes/{id}` | `GET`       | `200 OK` |

| ACTION                              | RESPONSE                                                                                                            | STATUS CODE        |
|-------------------------------------|---------------------------------------------------------------------------------------------------------------------|--------------------|
| - _Is not authenticated_            | _void_                                                                                                              | `401 Unauthorized` |
| - _Try to see a note of other user_ | _void_                                                                                                              | `404 Not Found`    |
|                                     |                                                                                                                     |                    |
| - ANY other exception(unhandled)    | _any of defaults([Cons.ExceptionHandler.defMsg](src/main/java/org/cris6h16/apirestspringboot/Constants/Cons.java))_ | `400 Bad Request`  |      

### 2.6 UPDATE/CREATE a NOTE (pass the id)

| URI              | HTTP METHOD | SUCCESS          |
|------------------|-------------|------------------|
| `api/notes/{id}` | `PUT`       | `204 NO CONTENT` |

| Action / Passing [CreateNoteDTO](src/main/java/org/cris6h16/apirestspringboot/DTOs/CreateNoteDTO.java) in `/{id}` | RESPONSE                                                                                                            | STATUS CODE        |
|-------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|--------------------| 
| - _`Title` is null_                                                                                               | - `Title is required`                                                                                               | `400 Bad Request`  |
| - _`Title` has just white spaces_                                                                                 | - `Title is required`                                                                                               |                    |
| - _`Title` is greater than 255 characters_                                                                        | - `Title must be less than 255 characters`                                                                          |                    |
| - _Is not authenticated_                                                                                          | _void_                                                                                                              | `401 Unauthorized` |
|                                                                                                                   |                                                                                                                     |                    |
| - ANY other exception(unhandled)                                                                                  | _any of defaults([Cons.ExceptionHandler.defMsg](src/main/java/org/cris6h16/apirestspringboot/Constants/Cons.java))_ | `400 Bad Request`  |      

### 2.7 DELETE a NOTE

| URI              | HTTP METHOD | SUCCESS          |
|------------------|-------------|------------------|
| `api/notes/{id}` | `DELETE`    | `204 NO CONTENT` |

| ACTION                                                                                                      | RESPONSE                                                                                                            | STATUS CODE        |
|-------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|--------------------|
| - Is not authenticated                                                                                      | _void_                                                                                                              | `401 Unauthorized` |
| - Try to delete an non-existent note                                                                        | - `Note not found`                                                                                                  | `404 Not Found`    |
| - Try to delete a note of other user( based on the retrieved by  _`findByUserId(...)`_ -> return nothing )_ |                                                                                                                     |                    |
|                                                                                                             |                                                                                                                     |                    |
| - ANY other exception(unhandled)                                                                            | _any of defaults([Cons.ExceptionHandler.defMsg](src/main/java/org/cris6h16/apirestspringboot/Constants/Cons.java))_ | `400 Bad Request`  |      

<hr>

### 2.8 LIST all USERS

| URI                                                                            | HTTP METHOD | SUCCESS  |
|--------------------------------------------------------------------------------|-------------|----------|
| `/api/users`<br/>- `page=<1>`<br/>- `size=<2>`<br/>- `sort=<create_at>, <asc>` | `GET`       | `200 OK` |

| Action                           | RESPONSE                                                                                                            | STATUS CODE        |
|----------------------------------|---------------------------------------------------------------------------------------------------------------------|--------------------|
| - _Is not authenticated_         | _void_                                                                                                              | `401 Unauthorized` |
| - _Is not an admin_              | _void_                                                                                                              | `403 Forbidden`    |
|                                  |                                                                                                                     |                    |
| - ANY other exception(unhandled) | _any of defaults([Cons.ExceptionHandler.defMsg](src/main/java/org/cris6h16/apirestspringboot/Constants/Cons.java))_ | `400 Bad Request`  |           

### 2.9 DELETE a USER

| URI          | HTTP METHOD | SUCCESS          |
|--------------|-------------|------------------|
| `/api/users` | `DELETE`    | `204 NO CONTENT` |

| Action                                 | RESPONSE                                                                                                            | STATUS CODE        |
|----------------------------------------|---------------------------------------------------------------------------------------------------------------------|--------------------|
| - _Try to delete other user's account_ | _void_                                                                                                              | `403 Forbidden`    |
| - _Is not authenticated_               | _void_                                                                                                              | `401 Unauthorized` |
|                                        |                                                                                                                     |                    |
| - _ANY other exception(unhandled)_     | _any of defaults([Cons.ExceptionHandler.defMsg](src/main/java/org/cris6h16/apirestspringboot/Constants/Cons.java))_ | `400 Bad Request`  |      

[//]: # (todo: docs the 100% of coverage reached)
[//]: # (![img.png]&#40;img.png&#41; )
[//]: # (![img_1.png]&#40;img_1.png&#41; )