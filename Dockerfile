# ── STAGE 1: Build the JAR ──────────────────────────────
# Use Maven + Java 17 to compile and package your app
FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first (so dependencies are cached if code doesn't change)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ── STAGE 2: Run the JAR ────────────────────────────────
# Use lightweight Java 17 runtime only (no Maven needed here)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Render sets PORT env variable — Spring reads it via ${PORT:8080}
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]