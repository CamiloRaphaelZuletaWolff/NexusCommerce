# syntax=docker/dockerfile:1

# ---- Build stage (canonical JDK 21) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
# Warm the dependency cache separately from source for better layer reuse.
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline
COPY src ./src
# Tests run in CI; skip here to keep image builds fast and deterministic.
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests clean package

# ---- Runtime stage (slim JRE, non-root) ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
RUN groupadd --system nexus && useradd --system --gid nexus --home /app nexus
COPY --from=build /workspace/target/nexuscommerce-backend.jar app.jar
USER nexus
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
