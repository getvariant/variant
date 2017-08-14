#! /bin/bash

cd `dirname $0`/../../SERVER

rm -rf target/universal/stage/RUNNING_PID
lsof -n -i4TCP:5377 | grep LISTEN | awk '{print $2}' | xargs kill

sbt "testProd -Dvariant.config.file=$1 -Dhttp.port=5377"
