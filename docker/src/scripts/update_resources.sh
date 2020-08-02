#!/bin/bash

# Run this script in order to process resources required for this deployment

if ! command -v envsubst &> /dev/null; then
  echo >&2 "Command envsubst isn't available and must be installed"
  exit 1
fi

cd "$(dirname $BASH_SOURCE)"
cd ..

. .env
export $(cat .env | grep -v -E '^\s*#.+' | cut -d= -f1)

# MariaDB
cd resources/mariadb
if [ ! -d "init" ]; then
  mkdir "init"
fi

envsubst < "templates/init.sql" > "init/init.sql"
