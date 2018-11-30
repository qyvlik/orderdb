
FROM maven:alpine as builder
COPY pom.xml pom.xml
COPY src/ src/
VOLUME /var/maven/.m2
RUN mvn -DskipTests clean package

#FROM frolvlad/alpine-oraclejdk8
FROM library/openjdk:8-alpine
MAINTAINER "<qyvlik@qq.com>"
WORKDIR /home/www
RUN adduser -D -u 1000 www www \
    && chown www:www -R /home/www
COPY --from=builder target/*.jar app.jar
EXPOSE 8080
USER www
ENTRYPOINT ["java", "-jar", "/home/www/app.jar"]