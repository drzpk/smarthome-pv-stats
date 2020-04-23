FROM openjdk:8u252-jre
LABEL maintainer="dominik.1.rzepka@gmail.com"

EXPOSE 8080

COPY docker-entrypoint.sh /
COPY application.war /app/

ENTRYPOINT /docker-entrypoint.sh