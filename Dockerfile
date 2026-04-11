FROM amazoncorretto:17 AS builder
WORKDIR /build

COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle build.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

FROM amazoncorretto:17-alpine
WORKDIR /app

ARG PROJECT_NAME=monew
ARG PROJECT_VERSION=0.0.1-SNAPSHOT
ENV PROJECT_NAME=${PROJECT_NAME}
ENV PROJECT_VERSION=${PROJECT_VERSION}
ENV JVM_OPTS=""
ENV SPRING_PROFILES_ACTIVE=prod

RUN addgroup -S monew && adduser -S monew -G monew
COPY --from=builder /build/build/libs/*.jar /app/app.jar
RUN chown -R monew:monew /app
USER monew

EXPOSE 80
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar /app/app.jar"]
