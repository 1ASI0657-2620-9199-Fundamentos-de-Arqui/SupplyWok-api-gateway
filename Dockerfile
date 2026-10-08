# Dockerfile for api-gateway
FROM maven:3.9.16-eclipse-temurin-26 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -f pom.xml -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:26-jre-noble AS runtime
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
