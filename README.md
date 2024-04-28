# api-rest-spring-boot-basic-auth-TDD--notes-app

Implementing a Semantic API REST in Spring Boot, using basic auth applying TDD, this is a little Note Application which
use PostgresSQL.

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

### 1.2 Endpoints

| URI                                                    | HTTP METHOD | SUCCESS          | DESC             | Authority    |
|--------------------------------------------------------|-------------|------------------|------------------|--------------|
| `/api/users`                                           | `POST`      | `201 CREATED`    | _create a user_  |              |
| `/api/users`                                           | `PATCH`     | `204 NO CONTENT` | _update a user_  | `ROLE_USER`  |         
| `/api/users`                                           | `DELETE`    | `204 NO CONTENT` | _delete a user_  | `ROLE_USER`  |
| `/api/notes`                                           | `POST`      | `201 CREATED`    | _create a note_  | `ROLE_USER`  |
| `/api/notes?page=<1>&size=<2>&sort=<create_at>, <asc>` | `GET`       | `200 OK`         | _list all notes_ | `ROLE_USER`  |
| `/api/notes/{id}`                                      | `GET`       | `200 OK`         | _see a note_     | `ROLE_USER`  |
| `/api/notes/{id}`                                      | `PUT`       | `204 NO CONTENT` | _update a note_  | `ROLE_USER`  |
| `/api/notes/{id}`                                      | `DELETE`    | `204 NO CONTENT` | _delete a note_  | `ROLE_USER`  |
| `/api/users?page=<1>&size=<2>&sort=<create_at>, <asc>` | `GET`       | `200 OK`         | _list all users_ | `ROLE_ADMIN` |

### 1.3 Entities

- User
    - id
    - username (max=20 & min=1)
    - email
    - password (min=8)
    - created_at
    - updated_at
    - deleted_at
    - Roles
    - notes
    - roles


- Note
    - id
    - title
    - content
    - created_at
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

| FAIL                                                                                                                                   | RESPONSE          |
|----------------------------------------------------------------------------------------------------------------------------------------|-------------------|
| - _Username already exists_<br/>- _Email already exists_                                                                               | `409 Conflict`    |
| - _Password is too short_<br/>- _Email is invalid_<br/>- _Email, Username and Password is required_<br/>- _Username length is invalid_ | `400 Bad Request` |

### 2.2 UPDATE a USER

| URI          | HTTP METHOD | SUCCESS          |
|--------------|-------------|------------------|
| `/api/users` | `PATCH`     | `204 NO CONTENT` |

| FAIL                                                                                                                                               | RESPONSE           |
|----------------------------------------------------------------------------------------------------------------------------------------------------|--------------------|
| - _Username already exists_<br/>- _Email already exists_<br/>- _Password is too short_<br/>- _Email is invalid_<br/>- _Username length is invalid_ | `409 Conflict`     |
| - _You need to be authenticated to perform this action_<br/>- _You cannot updated other users Accounts_                                            | `401 Unauthorized` |

<hr>  

### 2.3 CREATE a NOTE

| URI          | HTTP METHOD | SUCCESS       |
|--------------|-------------|---------------|
| `/api/notes` | `POST`      | `201 CREATED` |

| FAIL                                                    | RESPONSE           |
|---------------------------------------------------------|--------------------|
| - _Title and Content is required_                       | `400 Bad Request`  |
| - _You need to be authenticated to perform this action_ | `401 Unauthorized` |

### 2.4 LIST all NOTES

| URI                                                    | HTTP METHOD | SUCCESS  |
|--------------------------------------------------------|-------------|----------|
| `/api/notes?page=<1>&size=<2>&sort=<create_at>, <asc>` | `GET`       | `200 OK` |

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

### 2.6 UPDATE a NOTE

## Time taken (UTC-5)

1. Setup project

| Date       | Defined | Start | End   |
|------------|---------|-------|-------|
| 2024-04-28 |         | 11:50 | 12:21 |

2. [DEFINING THE PROJECT](#1-defining-the-project)

| Date       | Defined                                                       | Start | End   |
|------------|---------------------------------------------------------------|-------|-------|
| 2024-04-28 | [User Case & Endpoints](#11-user-case)                        | 12:47 | 13:12 |
| 2024-04-28 | [Entities](#13-entities) & Corrections(endpoints, error code) | 13:30 | 14:00 |

3. [API REST](#2-API-REST)

| Date       | Defined                                                                                         | Start | End   |
|------------|-------------------------------------------------------------------------------------------------|-------|-------|
| 2024-04-28 | [USER CREATION](#21-create-a-user)                                                              | 12:24 | 12:47 |
| 2024-04-28 | [UPDATED A USER](#22-update-a-user)                                                             | 13:15 | 13:30 |
| 2024-04-28 | [OPERATIONS WITH NOTES](#23-create-a-note) & Corrections in [UPDATED A USER](#22-update-a-user) | 14:00 | 14:30 |
| 2024-04-28 | conflict                                                                                        | 14:30 | 14:41 |
