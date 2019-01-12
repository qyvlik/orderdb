
FROM maven:alpine as builder
COPY pom.xml pom.xml
COPY src/ src/
VOLUME /var/maven/.m2
RUN mvn -DskipTests clean package

FROM frolvlad/alpine-oraclejdk8
MAINTAINER "test <qyvlik@qq.com>"
WORKDIR /home/www
VOLUME /tmp
VOLUME /home/www/orderdb
ADD target/*.jar /home/www/app.jar
RUN adduser -D -u 1000 www www \
    && chown www:www -R /home/www
EXPOSE 8080
USER www
ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -jar /home/www/app.jar
