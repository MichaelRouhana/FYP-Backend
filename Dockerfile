# Multi-stage build: First stage compiles, second stage runs
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy pom.xml first (for better caching)
COPY pom.xml .

# Download dependencies (better caching)
RUN mvn dependency:go-offline -B || true

# Copy source code
COPY src ./src

# Build the application (this will compile with the NEW TeamStatsDTO)
RUN mvn clean package -DskipTests -B

# Second stage: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/FYP-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
