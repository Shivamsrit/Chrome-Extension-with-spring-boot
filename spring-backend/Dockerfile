# ── Stage 1: Dependency cache ─────────────────────────────────────────────────
# Copy only pom.xml first so Maven deps are cached unless pom changes.
FROM maven:3.9-eclipse-temurin-21 AS deps
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q

# ── Stage 2: Build ────────────────────────────────────────────────────────────
FROM deps AS build
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 3: Lightweight production runtime ───────────────────────────────────
FROM eclipse-temurin:21-jre AS runtime

# OCI image labels (update version on each release)
LABEL org.opencontainers.image.title="quick-answer-backend" \
      org.opencontainers.image.description="QuickAnswer Chrome Extension — Spring Boot AI backend" \
      org.opencontainers.image.version="1.0.0" \
      org.opencontainers.image.licenses="MIT"

# Run as non-root for security
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080

# JVM flags tuned for containers:
#   UseContainerSupport  — respect cgroup CPU/memory limits
#   MaxRAMPercentage     — use up to 75% of container RAM for heap
#   ExitOnOutOfMemoryError — fail fast instead of hanging
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", "app.jar"]