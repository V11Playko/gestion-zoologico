# Stage 1: build the fat JAR using the project's own Gradle wrapper
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew .
COPY gradle/ gradle/
RUN chmod +x gradlew

# Copy build files first to cache the dependency download layer
COPY build.gradle settings.gradle ./
COPY messaging-service/build.gradle messaging-service/
RUN ./gradlew :messaging-service:dependencies --no-daemon -q 2>/dev/null || true

# Copy source and build (skip tests)
COPY messaging-service/src/ messaging-service/src/
RUN ./gradlew :messaging-service:bootJar --no-daemon -x test

# Stage 2: minimal runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /workspace/messaging-service/build/libs/messaging-service-*.jar app.jar

EXPOSE 8092

# -Xmx350m keeps memory under control on a t3.micro alongside zoologico-service
ENTRYPOINT ["java", "-Xmx350m", "-jar", "app.jar"]
