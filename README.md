# api-rest-spring-boot-100%-test-coverage-postgresql 

This repository shows you how I was putting in practice my knowledge about Spring Boot...   
I propose myself write a REST API with Spring Boot, and reach the 100% of test coverage.
Here I wrote from 0 a Secure Notes APP API & 100% Tested, this also save all exceptions 
in a log file, also the authentication failures & the successful...   
Due to the fact that I implemented this with basic authentication, I won't document widely the API...

# DOCS

The unique documentation that I implemented is with SpringDoc OpenAPI, you can see the API documentation in the following URL:
- `http://localhost:8080/docs/swagger-ui.html`   
You need run the API to see the documentation...

# TEST THIS API

1. Create a local DB in PostgresSQL:
    - `port`: `5432`
    - `database`: `api-rest-spring-boot`
    - `schema`: 
      - `tests`: used for the tests
      - `public`: used for the app in production mode

2. Add the Environment variables:
    - `PSQL_PASS`: password of the user which has the grants for the `api-rest-spring-boot` database
    - `PSQL_USER`: username of the user which has the grants for the `api-rest-spring-boot` database

3. Verify the above:
    - `echo $PSQL_USER && echo $PSQL_PASS`: if you see the credentials, then you can continue
    - `psql -h localhost -p 5432 -U $PSQL_USER -d api-rest-spring-boot -c "SELECT schema_name FROM information_schema.schemata;"`:
      if you can see the schema `tests` && `public`, then you can continue.

4. JDK:
    - `21`


# PD: 
Dockerize this API is in my TODO list...

[//]: # (todo: improve docs, add some diagrams, etc; but this as final step)


[//]: # (todo: docs the 100% of coverage reached)
[//]: # (![img.png]&#40;img.png&#41; )
[//]: # (![img_1.png]&#40;img_1.png&#41; )
[//]: # (doc about how we can optimize the code, for example )

[//]: # (not let reach to database exceptions we can validate )

[//]: # (the data before to send it to the database,)

[//]: # (&#40; i.g sort by a field that doesn't exist, find by id <0, etc&#41;)
 
