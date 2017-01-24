#! /bin/bash
# Make and package a Variant release
#
# 1. Core jar 

#!/bin/bash

export version=0.7.0

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
mkdir ${stage_dir} ${target_dir}

#
# CORE
#
cd ${workspace_root_dir}/CORE
mvn clean package -DskipTests
#cp $workspace_root_dir/CORE/target/variant-core*.jar ${stage_dir}

#
# SERVER
#
cd ${workspace_root_dir}/SERVER
sbt clean dist
mv target/universal/variant-${version}.zip ${stage_dir}
cd ${stage_dir}
unzip variant-${version}.zip
rm variant-${version}.zip
cd variant-${version}
rm -rf README share
cp -r ${workspace_root_dir}/SERVER/schemas .
cp ${workspace_root_dir}/SERVER/conf/variant.sh .
cd ..
zip -r variant-${version}.zip variant-${version}/
rm -rf variant-${version}

#
# JAVA CLIENT
#
cd ${workspace_root_dir}/CLIENT-JAVA
mvn clean package -DskipTests
cp target/variant-java-client*.jar ${stage_dir}

#
# JAVA SERVLET ADAPTER
#
cd ${workspace_root_dir}/CLIENT-JAVA-SERVLET
mvn clean package -DskipTests
cp target/variant-java-client-servlet-adapter*.jar ${stage_dir}

#
# JAVASCRIPT CLIENT
#
${workspace_root_dir}/CLIENT-JS/bin/package.sh
cp ${workspace_root_dir}/CLIENT-JS/target/variant*.js ${target_dir}

#
# DB
#
mkdir -p ${stage_dir}/db/postgres ${stage_dir}/db/h2
cp ${workspace_root_dir}/CORE/src/main/resources/variant/*schema.sql ${stage_dir}/db/postgres
cp ${workspace_root_dir}/CORE/src/main/resources/variant/*schema.sql ${stage_dir}/db/h2

#
# PACKAGE
#
cd ${stage_dir}
zip ${target_dir}/variant-${version}-all.zip * #./*.jar ./*.war ./*.zip

#
# SERVLET DEMO
# Separate file because WP doesn't take files > 50M
# Zip up the demo WAR because WP doesn't take WAR files. 
#
cd ${workspace_root_dir}/CLIENT-JAVA-SERVLET-DEMO
mvn clean package -DskipTests
cd target
zip ${target_dir}/variant-${version}-java-servlet-demo.zip petclinic.war

#
# Javadoc
#
${release_dir}/bin/javadoc.sh