# Stage 1: Build JAR with Eclipse Temurin JDK 23
FROM eclipse-temurin:23-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper & pom.xml for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build production jar
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal Runtime Image
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app

# Run as non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Pre-create uploads directory and ensure appuser owns /app directory
RUN mkdir -p /app/uploads/resources && chown -R appuser:appgroup /app

USER appuser

COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
