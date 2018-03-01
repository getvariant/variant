#! /bin/bash
# Package server
#

#!/bin/bash

export version=0.8.1
export version2=""

function usage() {
    echo "$(basename $0) email"
} 

if [[ x != "x$1" ]]; then
    usage
    exit 1
fi

workspace_root_dir=$(cd $(dirname $0)/../..; pwd)

release_dir=${workspace_root_dir}/RELEASE
stage_dir=${release_dir}/stage
target_dir=${release_dir}/target

cd ${workspace_root_dir}/SERVER
sbt clean dist
cd target/universal
unzip variant-${version}.zip 
rm variant-${version}.zip
cd variant-${version}
rm -rf README share
cp -r ${workspace_root_dir}/SERVER/distr/schemata .
cp -r ${workspace_root_dir}/SERVER/distr/ext .
cp -r ${workspace_root_dir}/SERVER/distr/db .
cp ${workspace_root_dir}/SERVER/distr/bin/variant.sh bin
mv bin/variant bin/playapp
cp lib/variant.variant-${version}-sans-externalized.jar ../variant-server-extapi-${version}.jar
cd ..
mv variant-${version} variant-server-${version}
zip -r variant-server-${version}.zip variant-server-${version}/
rm -rf variant-server-${version}
