#!/bin/bash

export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=development
export DB_USER=development
export DB_PASSWORD=development
lein clean && lein with-profile dev run --db-host $DB_HOST --db-port $DB_PORT --db-name $DB_NAME --db-user $DB_USER --db-pass $DB_PASSWORD

