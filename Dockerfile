FROM gradle:jdk21 AS build
COPY --chown=gradle:gradle . /src
WORKDIR /src

RUN ./gradlew bootJar


FROM openjdk:21-jdk as runtime
WORKDIR /app
COPY --from=build /src/build/libs/app.jar app.jar
COPY --from=build /src/build/agent/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
COPY --from=build /src/build/agent/opentelemetry-javaagent-extension.jar /app/opentelemetry-javaagent-extension.jar
EXPOSE 8080

ENTRYPOINT java -javaagent:/app/opentelemetry-javaagent.jar -Dotel.javaagent.extensions=/app/opentelemetry-javaagent-extension.jar -jar /app/app.jar