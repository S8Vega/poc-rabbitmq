FROM eclipse-temurin:17.0.14_7-jdk-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup -s /bin/sh

COPY poc-rabbitmq.jar app.jar

ENV RABBITMQ_HOST=host.containers.internal
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

USER appuser

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
