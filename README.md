# Jackformer

Web app for flexible data transforms.

## Build, run

To build locally, use Maven, then run as [Spring Boot](https://spring.io/projects/spring-boot) app:

    mvn clean spring-boot:run

and the default app will be available on localhost port 9090.

Alternatively there is rudimentary `Dockerfile` for something like:

    docker build -t mydockerhubrepo/jackformer .
    docker run -p 9090:9090 mydockerhubrepo/jackformer

## Formats supported



