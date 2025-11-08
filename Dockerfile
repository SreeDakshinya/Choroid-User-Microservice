# ---- Builder stage ----
FROM eclipse-temurin:11-jdk AS build
WORKDIR /app

# Gradle wrapper and build scripts first (better layer caching)
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts build.gradle.kts ./
RUN chmod +x gradlew

# Warm the Gradle dependency cache (won't fail build if offline)
RUN ./gradlew --no-daemon help || true

# App sources
COPY src src

# Build fat jar
RUN ./gradlew --no-daemon clean bootJar -x test

# ---- Runtime stage ----
FROM eclipse-temurin:11-jre

# Non-root user
RUN addgroup --system spring && adduser --system --ingroup spring spring
WORKDIR /app

# Copy the boot jar from builder
COPY --from=build /app/build/libs/*SNAPSHOT.jar /app/app.jar

# Expose app port and Spark UI (if enabled)
EXPOSE 8081 4040

# Common tunables
ENV JAVA_OPTS="" \
    SERVER_PORT=8081 \
    SPRING_PROFILES_ACTIVE=default

# If your H2 TCP server runs on the host, override SPRING_DATASOURCE_URL at runtime, e.g.:
#   -e SPRING_DATASOURCE_URL=jdbc:h2:tcp://host.docker.internal:9092/choroid;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;IFEXISTS=FALSE

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${SERVER_PORT} -jar /app/app.jar"]
