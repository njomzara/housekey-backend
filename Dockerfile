FROM maven:3.9.6-eclipse-temurin-21

WORKDIR /app

# Copy only the Maven descriptor first so dependency layers can be reused.
COPY pom.xml .

EXPOSE 8080 5005

CMD ["mvn", "-ntp", "spring-boot:run"]
