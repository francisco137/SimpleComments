#!/bin/bash

PG_VER=$1
ROOT_DIR=$(cd "$(dirname "$0")" ; pwd -P)
cd $ROOT_DIR

pg_ctlcluster $PG_VER main start
sbt run
