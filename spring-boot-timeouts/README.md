# CockroachDB Spring Boot :: Transaction Timeouts Demo

A standalone spring boot app demonstrating setting server-side transaction timeouts via AOP.

## Relevant Articles

- [Blogs on CockroachDB with the Java Stack](https://blog.cloudneutral.se/)
  
## Running Tests

```shell
../mvnw -DskipTests=false -Dtest=io.roach.spring.timeouts.TimeoutsTest -Dspring.profiles.active=default -Dspring.datasource.url=jdbc:postgresql://localhost:26257/spring_boot_demo?sslmode=disable -Dspring.datasource.username=root -Dspring.datasource.password=root test
```
   
### Misc

When using JDK 11 and connecting against a secure CockroachDB cluster 
then set the following VM option:

    -Djdk.tls.client.protocols=TLSv1.2
