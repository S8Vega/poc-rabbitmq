# Etapa 1
FROM gradle:8.13-jdk17-alpine AS build
WORKDIR /app

COPY . .

RUN gradle clean build -x test
RUN ls -R /app

# Etapa 2: Imagen final solo con la aplicación compilada
FROM eclipse-temurin:17.0.14_7-jdk-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup -s /bin/sh

COPY --from=build /app/applications/app-service/build/libs/*.jar /app/app.jar

ENV RABBITMQ_HOST=host.containers.internal
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

USER appuser

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]