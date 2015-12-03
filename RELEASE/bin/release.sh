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

version="0.5.RC-6"

workspace_root_dir=$(pwd)/$(dirname $0)/../..
release_dir=${workspace_root_dir}/RELEASE
stage_dir=${release_dir}/stage
out_dir=${release_dir}/out

rm -rf ${stage_dir}/*
rm -rf ${out_dir}/*

#
# Core
#
cd ${workspace_root_dir}/CORE
mvn clean package -DskipTests

cp $workspace_root_dir/CORE/target/variant-core*.jar ${stage_dir}

#
# Web
#

cd ${workspace_root_dir}/WEB
mvn clean package -DskipTests

cp $workspace_root_dir/WEB/target/variant-web*.jar ${stage_dir}

#
# Web Sample
#
cd ${workspace_root_dir}/WEB-SAMPLE
mvn clean package -DskipTests
cp -R $workspace_root_dir/WEB-SAMPLE ${stage_dir}
cd ${stage_dir}/WEB-SAMPLE
rm -rf .classpath .project .settings target
tar -cvf ${stage_dir}/spring-petclinic-variant.tar ./*

#
# Package
#
cd ${stage_dir}
tar -cvf ${out_dir}/variant-all-${version}.tar ./*.jar ./*.tar
