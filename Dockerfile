FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/healthcare-0.0.1-SNAPSHOT.jar healthcare.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "healthcare.jar"]