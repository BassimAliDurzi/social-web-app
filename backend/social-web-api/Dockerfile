# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy Maven wrapper + pom first for better layer caching
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Download dependencies (cacheable)
RUN ./mvnw -q -DskipTests dependency:go-offline

# Copy source
COPY src src

# Build jar (skip tests for Docker build speed; CI will run tests separately)
RUN ./mvnw -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy built jar
COPY --from=build /workspace/target/*.jar /app/app.jar

# Document default port (actual port controlled by env PORT; default in app is 8081)
EXPOSE 8081

# Run
ENTRYPOINT ["java", "-jar", "/app/app.jar"]