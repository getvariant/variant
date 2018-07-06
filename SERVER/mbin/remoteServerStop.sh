#! /bin/bash

cd `dirname $0`/../../SERVER
rootdir=$(pwd)

lsof -n -i4TCP:5377 | grep LISTEN | awk '{print $2}' | xargs kill
rm -rf target/universal/stage/RUNNING_PID
