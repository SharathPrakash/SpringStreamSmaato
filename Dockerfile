FROM openjdk:8
MAINTAINER "Sharath Prakash <sharath.prakash1992@gmail.com>"
WORKDIR /app

COPY ./target/*.jar ./smaato.jar
ENTRYPOINT ["java", "-jar", "/app/smaato.jar"]

EXPOSE 8080