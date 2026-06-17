FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN chmod +x ./gradlew && ./gradlew build -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
