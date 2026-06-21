FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy only the Maven descriptor first so dependency layers can be reused.
COPY pom.xml .
RUN mvn -ntp dependency:go-offline

COPY src ./src
RUN mvn -ntp package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=render

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
