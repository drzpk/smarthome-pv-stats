FROM openjdk:8u252-jre-slim
LABEL maintainer="dominik.1.rzepka@gmail.com"

EXPOSE 8080

ENTRYPOINT /docker-entrypoint.sh
WORKDIR /app

COPY docker-entrypoint.sh /
COPY application.war /app/