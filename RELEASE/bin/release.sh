#! /bin/bash
# Make and package a Variant release
#
# 1. Core jar 

#!/bin/bash

export version=0.7.1
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

rm -rf ${stage_dir} ${target_dir}
mkdir ${stage_dir} ${target_dir} ${stage_dir}/server ${stage_dir}/java

#
# CORE
#
${workspace_root_dir}/CORE/bin/release.sh
cp $workspace_root_dir/CORE/target/core*.jar ${stage_dir}/java

#
# SERVER
#
${workspace_root_dir}/SERVER/bin/release.sh
cp $workspace_root_dir/SERVER/target/universal/variant-${version}-server.zip ${stage_dir}/server/variant-${version}${version2}-server.zip

#
# JAVA CLIENT
#
cd ${workspace_root_dir}/CLIENT-JAVA
mvn clean package -DskipTests
cp target/java-client*.jar ${stage_dir}/java
cp distr/variant.conf ${stage_dir}/java

#
# PACKAGE
#

cd ${stage_dir}/java
zip ${target_dir}/variant-${version}${version2}-java.zip *
cd ..
rm -rf java
mv ${stage_dir}/server/* ${target_dir}

#
# JAVASCRIPT CLIENT
#
${workspace_root_dir}/CLIENT-JS/bin/package.sh
cp ${workspace_root_dir}/CLIENT-JS/target/variant*.js ${target_dir}/variant-${version}${version2}.js

#
# Javadoc
#
#${release_dir}/bin/javadoc.sh

exit

#
# !!!!! SERVLET ADAPTER AND DEMO MOVED TO SEPARATE REPO !!!!
# Separate file because WP doesn't take files > 50M
# Zip up the demo WAR because WP doesn't take WAR files. 
#
cd ${workspace_root_dir}/CLIENT-JAVA-SERVLET
mvn clean package -DskipTests
cp target/variant-java-client-servlet-adapter*.jar ${stage_dir}/java

cd ${workspace_root_dir}/CLIENT-JAVA-SERVLET-DEMO
mvn clean package -DskipTests
cd target
zip ${target_dir}/variant-${version}${version2}-java-servlet-demo.zip petclinic.war
