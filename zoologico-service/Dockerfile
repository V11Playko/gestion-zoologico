# Stage 1: build the fat JAR using the project's own Gradle wrapper
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew .
COPY gradle/ gradle/
RUN chmod +x gradlew

# Copy build files first to cache the dependency download layer
COPY build.gradle settings.gradle ./
COPY zoologico-service/build.gradle zoologico-service/
RUN ./gradlew :zoologico-service:dependencies --no-daemon -q 2>/dev/null || true

# Copy source and build (skip tests — they require a live DB)
COPY zoologico-service/src/ zoologico-service/src/
RUN ./gradlew :zoologico-service:bootJar --no-daemon -x test

# Stage 2: minimal runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /workspace/zoologico-service/build/libs/zoologico-service-*.jar app.jar

EXPOSE 8091

# -Xmx400m keeps memory under control on a t3.micro (1 GB total RAM)
ENTRYPOINT ["java", "-Xmx400m", "-jar", "app.jar"]
