# ---- build stage: compile the Spring Boot API ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY backend/pom.xml .
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
RUN mvn -q -B -DskipTests package

# ---- runtime stage: API + static frontend on one origin ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/apex-innovators-backend-1.0.0.jar /app/api.jar
COPY frontend /app/frontend
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=render \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["/app/entrypoint.sh"]
