FROM openjdk:17-slim AS builder

WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew
RUN ./gradlew build --no-daemon

FROM openjdk:17-slim

WORKDIR /app

COPY --from=builder /build/build/libs/*.jar conal-back.jar

CMD ["/bin/sh", "-c", "java -Dspring.profiles.active=prod -jar conal-back.jar"]
