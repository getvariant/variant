#! /bin/bash
# Make and package a Variant release
#
# 1. Core jar 

#!/bin/bash

export version=0.6.2

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
cp $workspace_root_dir/CORE/target/variant-core*.jar ${stage_dir}

#
# SERVER
#
cd ${workspace_root_dir}/SERVER-HTTP
sbt clean package
cp target/scala-2.11/variant-server*.war ${stage_dir}/variant-server-${version}.war

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
#(cd src/main/java; jar -cvf ${stage_dir}/variant-client-adapter-source-${version}.jar com/variant/client/adapter/*)

#
# JAVASCRIPT CLIENT
#
${workspace_root_dir}/CLIENT-JS/bin/package.sh
cp ${workspace_root_dir}/CLIENT-JS/target/variant*.js ${target_dir}

#
# WEB DEMO
#
cd ${workspace_root_dir}/WEB-DEMO
mvn clean package -DskipTests
cp -R ${workspace_root_dir}/WEB-DEMO ${stage_dir}
cd ${stage_dir}/WEB-DEMO
rm -rf .classpath .project .settings target
tar -cvf ${stage_dir}/variant-spring-petclinic.tar ./*
cd ${stage_dir}
rm -rf WEB-DEMO


#
# DB
#
mkdir -p ${stage_dir}/db/postgres ${stage_dir}/db/h2
cp ${workspace_root_dir}/CORE/src/main/resources/variant/*schema.sql ${stage_dir}/db/postgres
cp ${workspace_root_dir}/CORE/src/main/resources/variant/*schema.sql ${stage_dir}/db/h2

#
# Package
#
cd ${stage_dir}
tar -cvf ${target_dir}/variant-all-${version}.tar * #./*.jar ./*.war ./*.tar

#
# Javadoc
#
${release_dir}/bin/javadoc.sh