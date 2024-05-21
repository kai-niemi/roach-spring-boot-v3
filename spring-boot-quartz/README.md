# CockroachDB Spring Boot :: Quartz Demo

A standalone spring boot app demonstrating transactional and databse-driven
quartz scheduling. It showcases using separate datasources and transaction managers
for quartz and the application business functions (although using the same db).

## Building

    ../mvnw clean install

## Running

Using default configuration:

    java -jar target/spring-boot-quartz.jar 

Using application.yml overrides:

    java -jar target/spring-boot-quartz.jar --spring.profiles.active=default,verbose --spring.datasource.url=jdbc:postgresql://localhost:26257/spring_boot_demo?sslmode=disable --spring.datasource.username=root --spring.datasource.password=

