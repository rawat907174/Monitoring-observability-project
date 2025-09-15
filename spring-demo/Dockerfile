FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Prep deps
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Build & REPACKAGE (ensures executable Spring Boot jar w/ manifest)
COPY src ./src
RUN mvn -q -DskipTests package spring-boot:repackage

# Grab the OTel Java agent (no curl needed)
ARG OTEL_VERSION=1.32.0
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_VERSION}/opentelemetry-javaagent.jar /otel-javaagent.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/spring-demo-1.0.0.jar /app/app.jar
COPY --from=build /otel-javaagent.jar /otel-javaagent.jar

# Auto-instrument Spring via agent
ENV JAVA_TOOL_OPTIONS="-javaagent:/otel-javaagent.jar"

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
