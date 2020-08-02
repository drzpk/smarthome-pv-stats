#!/usr/bin/env sh

INCLUDE_LINE="Include conf/pv-stats/pv-stats.conf"
MAIN_CONFIG_FILE="/usr/local/apache2/conf/httpd.conf"

if ! cat "$MAIN_CONFIG_FILE" | grep "$INCLUDE_LINE"; then
  echo "Appending pv-stats HTTP configuration to main configuration file"
  /bin/echo -e "\n$INCLUDE_LINE\n" >> "$MAIN_CONFIG_FILE"
fi

httpd-foreground