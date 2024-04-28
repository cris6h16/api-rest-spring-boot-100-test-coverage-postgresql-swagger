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

### 1.2. Endpoints

| URI               | HTTP METHOD | SUCCESS          | DESC             |
|-------------------|-------------|------------------|------------------|
| `/api/users`      | `POST`      | `201 CREATED`    | _create a user_  |
| `/api/users`      | `PATCH`     | `204 NO CONTENT` | _update a user_  |
| `/api/users`      | `DELETE`    | `204 NO CONTENT` | _delete a user_  |
| `/api/notes`      | `POST`      | `201 CREATED`    | _create a note_  |
| `/api/notes`      | `GET`       | `200 OK`         | _list all notes_ |
| `/api/notes/{id}` | `GET`       | `200 OK`         | _see a note_     |
| `/api/notes/{id}` | `PUT`       | `204 NO CONTENT` | _update a note_  |
| `/api/notes/{id}` | `DELETE`    | `204 NO CONTENT` | _delete a note_  |
| `/api/users`      | `GET`       | `200 OK`         | _list all users_ |

### 1.3. Entities

- User
    - id
    - username (max=20 & min=1)
    - email
    - password (min=8)
    - created_at
    - updated_at
    - deleted_at
    - ERole
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

## 2. API REST

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
 `/api/users` | `PATCH`     | `204 NO CONTENT` |

| FAIL                                                                                                                                               | RESPONSE       |
|----------------------------------------------------------------------------------------------------------------------------------------------------|----------------|
| - _Username already exists_<br/>- _Email already exists_<br/>- _Password is too short_<br/>- _Email is invalid_<br/>- _Username length is invalid_ | `409 Conflict` |

<hr>  

### 2.3 CREATE a NOTE

| URI          | HTTP METHOD | SUCCESS       |
|--------------|-------------|---------------|
| `/api/notes` | `POST`      | `201 CREATED` |

| FAIL                              | RESPONSE          |
|-----------------------------------|-------------------|
| - _Title and Content is required_ | `400 Bad Request` |

### 2.4 LIST all NOTES

| URI          | HTTP METHOD | SUCCESS  |
|--------------|-------------|----------|
| `/api/notes` | `GET`       | `200 OK` |

## Time taken (UTC-5)

1. Setup project

| Date       | Defined | Start | End   |
|------------|---------|-------|-------|
| 2024-04-28 |         | 11:50 | 12:21 |

2. [DEFINING THE PROJECT](#1-defining-the-project)

| Date       | Defined                                   | Start | End   |
|------------|-------------------------------------------|-------|-------|
| 2024-04-28 | [User Case & Endpoints](#11-user-case)    | 12:47 | 13:12 |
| 2024-04-28 | [Entities](#12-entities) & Add error code | 13:30 | 13:46 |

3. [API REST](#API-REST)

| Date       | Defined                             | Start      | End   |
|------------|-------------------------------------|------------|-------|
| 2024-04-28 | [USER CREATION](#21-create-a-user)  | 12:24      | 12:47 |
| 2024-04-28 | [UPDATED A USER](#22-update-a-user) | 13:15<br/> | 13:30 |