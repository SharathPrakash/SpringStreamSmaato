FROM 8-jdk-oraclelinux7
MAINTAINER "Sharath Prakash <sharath.prakash1992@gmail.com>"
WORKDIR /app

COPY ./target/*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

EXPOSE 8080