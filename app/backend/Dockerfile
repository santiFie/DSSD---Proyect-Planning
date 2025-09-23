# --- Etapa 1: Build ---
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
RUN mvn dependency:go-offline
COPY src src
RUN mvn package -DskipTests

# --- Etapa 2: Run ---
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]