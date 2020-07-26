#!/bin/bash

BACKUP_DIR="../backup/grafana"

. ../.env

if [ ! -d $BACKUP_DIR ]; then
  mkdir $BACKUP_DIR
fi

filename="grafana_$(date '+%Y-%m-%d').tar.gz"
file="$BACKUP_DIR/$filename"

if [ -f $file ]; then
  echo "Backup file $file already exists and will be replaced"
fi

tar -zcf "$file" ../resources/grafana-data/*
