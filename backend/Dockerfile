
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app


COPY target/*.jar app.jar

EXPOSE 5050

ENTRYPOINT ["java", "-Duser.timezone=Europe/Bucharest", "-jar", "app.jar"]