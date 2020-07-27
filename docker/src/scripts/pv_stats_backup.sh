#!/bin/bash

BACKUP_DIR="../backup/pv_stats"

. ../.env

if [ ! -d $BACKUP_DIR ]; then
  mkdir $BACKUP_DIR
fi

filename="pv_stats_$(date '+%Y-%m-%d').sql"
file="$BACKUP_DIR/$filename"

if [ -f $file ]; then
  echo "Backup file $file already exists and will be replaced"
fi

docker exec mariadb mysqldump -uroot -proot_password_123_xyz456 --single-transaction --compress pv_stats > "$file"
