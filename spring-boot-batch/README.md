# CockroachDB Spring Boot :: Batch Demo

A standalone spring boot app demonstrating batch inserts and updates
using [Spring Data JPA](https://spring.io/projects/spring-data-jpa) and Hibernate.

Key takeaways:

- How to use and enable batching
- How to verify batching actually happens

## Relevant Articles

- [Multitenancy Applications with Spring Boot and CockroachDB](https://blog.cloudneutral.se/multitenancy-applications-with-spring-boot-and-cockroachdb)
- [Spring Annotations for CockroachDB](https://blog.cloudneutral.se/spring-annotations-for-cockroachdb)

  
## Running Tests

### Batch Statements

```shell
mvn -DskipTests=false -Dtest=io.roach.spring.batch.BatchStatementTest \
  -Dspring.profiles.active=default -Droach.multi-value-inserts=true -Droach.batch-size=64 \ 
  -Dspring.datasource.url=jdbc:postgresql://192.168.1.2:26257/spring_boot_demo?sslmode=disable \ 
  -Dspring.datasource.username=root \ 
  -Dspring.datasource.password=root \ 
  test
```

### Batch Upserts

```shell
mvn -DskipTests=false -Dtest=io.roach.spring.batch.BatchUpsertTest \
  -Dspring.profiles.active=default -Droach.multi-value-inserts=true -Droach.batch-size=64 \ 
  -Dspring.datasource.url=jdbc:postgresql://192.168.1.2:26257/spring_boot_demo?sslmode=disable \ 
  -Dspring.datasource.username=root \ 
  -Dspring.datasource.password=root \ 
  test
```
   
### Misc

When using JDK 11 and connecting against a secure CockroachDB cluster set the following VM option:

    -Djdk.tls.client.protocols=TLSv1.2
