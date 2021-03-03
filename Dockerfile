# From https://spring.io/guides/gs/spring-boot-docker/

# before docker build, need to do...
#
# mvn clean package
# mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)
#
# and then
#
# docker build -t [DOCKER-REPO]/jackformer-webapp .
# docker run -p 9090:9090 [DOCKER-REPO]/jackformer-webapp
#
# Note, however, that the better way to build the image is to just run:
#
# ./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=[DOCKER-REPO]/jackformer-webapp

FROM openjdk:8-jdk-alpine
RUN addgroup -S jack && adduser -S jack -G jack
USER jack:jack
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.cowtowncoder.jackformer.webapp.JackformerApp"]
