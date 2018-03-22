#! /bin/bash

cd `dirname $0`/../../SERVER
rootdir=$(pwd)

rm -rf target/universal/stage/RUNNING_PID
lsof -n -i4TCP:5377 | grep LISTEN | awk '{print $2}' | xargs kill

config=${1:-${rootdir}/conf-test/variant-testProd.conf}
ext_dir=${rootdir}/distr/ext
schemata_dir=${rootdir}/test-schemata
sbt "testProd -Dvariant.config.file=$config -Dvariant.schemata.dir=$schemata_dir -Dvariant.ext.dir=$ext_dir -Dhttp.port=5377"
