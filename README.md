git # api-rest-spring-boot-100%-test-coverage-postgresql 

DOCS STILL NOT ADDED, BUT IT'S A TO DO

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


[//]: # (todo: docs the 100% of coverage reached)
[//]: # (![img.png]&#40;img.png&#41; )
[//]: # (![img_1.png]&#40;img_1.png&#41; )
[//]: # (doc about how we can optimize the code, for example )

[//]: # (not let reach to database exceptions we can validate )

[//]: # (the data before to send it to the database,)

[//]: # (&#40; i.g sort by a field that doesn't exist, find by id <0, etc&#41;)
 
