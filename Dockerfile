# From https://spring.io/guides/gs/spring-boot-docker/

# before docker build, need to do...
#
# mvn clean package
# mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)
#
# and then
#
# docker build -t cowtowncoder/jackformer .
# docker run -p 9090:9090 cowtowncoder/jackformer

FROM openjdk:8-jdk-alpine
RUN addgroup -S jack && adduser -S jack -G jack
USER jack:jack
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.cowtowncoder.jackformer.webapp.JackformerApp"]
