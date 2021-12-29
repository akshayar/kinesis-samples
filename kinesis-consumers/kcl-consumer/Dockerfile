FROM openjdk:11
WORKDIR /app


COPY target/kcl-consumer-1.0.0.jar /app/app.jar
COPY src/main/config /app/config
USER root
ENV STREAM_NAME=aksh-first
ENTRYPOINT ["java", "-jar" ,"-DstreamName=${STREAM_NAME}" ,"/app/app.jar", "--spring.config.location=/app/config/application.properties --container-run"]
