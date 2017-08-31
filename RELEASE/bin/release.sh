#! /bin/bash
# Make and package a Variant release
#
# 1. Core jar 

#!/bin/bash

export version=0.8.0
export version2=""

function usage() {
    echo "$(basename $0) email"
} 

if [[ x != "x$1" ]]; then
    usage
    exit 1
fi

variant_root=$(cd $(dirname $0)/../..; pwd)
server_ext_root=$(cd ${variant_root}/../variant-server-extensions; pwd)
release_dir=${variant_root}/RELEASE
stage_dir=${release_dir}/stage
target_dir=${release_dir}/target

rm -rf ${stage_dir} ${target_dir}
mkdir ${stage_dir} ${target_dir} ${stage_dir}/server ${stage_dir}/java

#
# CORE
#
${variant_root}/CORE/bin/release.sh
cp $variant_root/CORE/target/variant-core*.jar ${stage_dir}/java
cp $variant_root/CORE/target/variant-core*.jar $server_ext_root/lib

#
# SERVER
#
${variant_root}/SERVER/bin/release.sh
cp $variant_root/SERVER/target/universal/variant-server-${version}.zip ${stage_dir}/server/variant-server-${version}${version2}.zip
cp $variant_root/SERVER/target/universal/variant-server-api-${version}.jar $server_ext_root/lib

#
# JAVA CLIENT
#
cd ${variant_root}/CLIENT-JAVA
mvn clean package -DskipTests
cp target/java-client*.jar ${stage_dir}/java/variant-java-client-${version}.jar
cp distr/variant.conf ${stage_dir}/java

#
# PACKAGE
#

cd ${stage_dir}/java
zip ${target_dir}/variant-java-${version}${version2}.zip *
cd ..
rm -rf java
mv ${stage_dir}/server/* ${target_dir}

#
# JAVASCRIPT CLIENT
#
${variant_root}/CLIENT-JS/bin/package.sh
cp ${variant_root}/CLIENT-JS/target/variant*.js ${target_dir}/variant-${version}${version2}.js

#
# Javadoc
#
#${release_dir}/bin/javadoc.sh

