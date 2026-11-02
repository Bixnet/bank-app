# Stage 1: build the jar
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY src ./src
COPY bank-api ./bank-api
WORKDIR /workspace/bank-api
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q -DskipTests package

# Stage 2: run it with a small Java runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/bank-api/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]