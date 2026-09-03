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
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=render \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "export JDBC_DATABASE_URL=$(echo "$DATABASE_URL" | sed -E "s#^postgres://#jdbc:postgresql://#"); echo "DB: $(echo "$DATABASE_URL" | sed -E "s#^postgres://[^@]*@##")"; exec java -jar /app/api.jar"]
