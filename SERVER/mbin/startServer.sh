#! /bin/bash

cd `dirname $0`/../../SERVER
rootdir=$(pwd)

rm -rf target/universal/stage/RUNNING_PID
lsof -n -i4TCP:5377 | grep LISTEN | awk '{print $2}' | xargs kill

config=${1:-${rootdir}/conf-test/variant-testProd.conf}
extdir=${rootdir}/distr/ext
sbt "testProd -Dvariant.config.file=$config -Dvariant.ext.dir=$extdir -Dhttp.port=5377"
