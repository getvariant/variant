#
# Make and package a Variant release
#
# 1. Core jar 

#!/bin/bash

set +e

function usage() {
    echo "$(basename $0) email"
} 

if [[ x != "x$1" ]]; then
    usage
    exit 1
fi

version="0.5.RC-2"

workspace_root_dir=$(pwd)/$(dirname $0)/../..
release_dir=${workspace_root_dir}/RELEASE
stage_dir=${release_dir}/stage
out_dir=${release_dir}/out

rm -rf ${stage_dir}/*

#
# Core
#
cd ${workspace_root_dir}/CORE
mvn clean install -DskipTests

cp $workspace_root_dir/CORE/target/variant-core-${version}.jar ${stage_dir}

#
# Web
#

cd ${workspace_root_dir}/WEB
mvn clean install -DskipTests

cp $workspace_root_dir/WEB/target/variant-web-${version}.jar ${stage_dir}


#
# Web Sample
#

cp -R $workspace_root_dir/SAMPLE-SMVC4 ${stage_dir}
cd ${stage_dir}/SAMPLE-SMVC4
rm -rf .classpath .project .settings target
tar -cvf ${stage_dir}/spring-petclinic-variant.tar ./*

#
# Package
#
cd ${stage_dir}
tar -cvf ${out_dir}/variant-all-${version}.tar ./*.jar ./*.tar
