FROM maven:3.9-eclipse-temurin-26 AS build-stage
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src src/
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:26-jre
WORKDIR /app

RUN apt-get update && apt-get install -y bash && rm -rf /var/lib/apt/lists/*

COPY --from=build-stage /app/target/*.jar app.jar
RUN mkdir -p /app/graph-cache

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]