#! /bin/bash

cd `dirname $0`/../../SERVER
rootdir=$(pwd)

rm -rf target/universal/stage/RUNNING_PID
lsof -n -i4TCP:5377 | grep LISTEN | awk '{print $2}' | xargs kill

config=${1:-${rootdir}/conf-test/remoteServer.conf}
ext_dir=${rootdir}/distr/ext
schemata_dir=/tmp/schemata-remote
sbt_arg="testProd -Dvariant.config.file=${rootdir}/conf-test/remoteServer.conf -Dvariant.schemata.dir=$schemata_dir -Dvariant.ext.dir=$ext_dir -Dhttp.port=5377 $1"
sbt "$sbt_arg"