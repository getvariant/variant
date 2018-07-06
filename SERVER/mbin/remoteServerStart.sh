#! /bin/bash

cd `dirname $0`/../../SERVER
rootdir=$(pwd)
echo "$1"
${rootdir}/mbin/remoteServerStop.sh

config=${1:-${rootdir}/conf-test/remoteServer.conf}
ext_dir=${rootdir}/distr/ext
schemata_dir=/tmp/schemata-remote
sbt_arg="testProd -Dvariant.config.file=${rootdir}/conf-test/remoteServer.conf -Dvariant.schemata.dir=$schemata_dir -Dvariant.ext.dir=$ext_dir -Dhttp.port=5377 $1"
echo "Executing [sbt $sbt_arg]"
sbt "$sbt_arg"