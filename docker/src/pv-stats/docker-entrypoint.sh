#!/usr/bin/env sh

java -Dlogging.config=/app/config/log4j2.xml ${PV_STATS_JAVA_OPTS} -jar /app/application.war