#!/bin/bash

ROOT_DIR=$(cd "$(dirname "$0")" ; pwd -P)
cd $ROOT_DIR

PG_VER="$1"
PG_HOST="$2"
PG_PORT="$3"
PG_BASE="$4"
PG_USER="$5"
PG_PASS="$6"
dir_name="$7"


useradd -m -p "${PG_PASS}" -s /bin/bash ${PG_USER}
echo "${PG_HOST}:${PG_PORT}:${PG_BASE}:${PG_USER}:${PG_PASS}" > /home/${PG_USER}/.pgpass
chown ${PG_USER}:${PG_USER} /home/${PG_USER}/.pgpass
chmod 600 /home/${PG_USER}/.pgpass

pg_ctlcluster ${PG_VER} main start
sleep 2

su - postgres -c "psql -c \"CREATE USER ${PG_USER} SUPERUSER;\" postgres"
su - postgres -c "psql -c \"ALTER USER ${PG_USER} PASSWORD '${PG_PASS}';\" postgres"
su - postgres -c "psql -c \"DROP DATABASE ${PG_BASE};\" postgres"
su - postgres -c "psql -c \"CREATE DATABASE ${PG_BASE} OWNER ${PG_USER};\" postgres"

su francisco_comments -c "psql ${PG_BASE} < conf/francisco_comments.sql"
su francisco_comments -c "psql ${PG_BASE} < data/francisco_comments_data.sql"

sed -i "s!jdbc:postgresql:.*!jdbc:postgresql://${PG_HOST}/${PG_BASE}?user=${PG_USER}\&password=${PG_PASS}\"!" /$dir_name/conf/application.conf
