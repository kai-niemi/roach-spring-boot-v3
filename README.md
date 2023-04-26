[![Java CI with Maven](https://github.com/kai-niemi/roach-spring-boot-v3/actions/workflows/maven.yml/badge.svg)](https://github.com/kai-niemi/roach-spring-boot-v3/actions/workflows/maven.yml)

# CockroachDB Spring Boot v3 Tutorials and Demos

Contains various Spring Boot v3+ code examples, tutorials and runnable demos for CockroachDB.

# Building and Installing

The demo projects are packaged as a single executable JAR files and runs on any platform for which there is a
Java 17+ runtime.

## Prerequisites

- Java 17
    - https://openjdk.org/projects/jdk/17/
    - https://www.oracle.com/java/technologies/downloads/#java17
- Maven 3+ (optional, embedded wrapper available)
    - https://maven.apache.org/

Install the JDK (Ubuntu example):

    sudo apt-get install openjdk-17-jdk

Confirm the installation by running:

    java --version

## Clone the project

    git clone git@github.com:kai-niemi/roach-spring-boot-v3

## Build the executable jar

    cd roach-spring-boot-v3
    chmod +x mvnw
    ./mvnw clean install

# Running

Create the database that will store job metadata and stats:

    cockroach sql --insecure --host=localhost -e "CREATE database spring_boot_demo"

# Terms of Use

See [MIT](LICENSE.txt) for terms and conditions.
