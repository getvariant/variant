#
# Make and package a Variant release
#
# 1. Core jar 

#!/bin/bash

export version=0.6.1
export full_version=${version}-RC1

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
out_dir=${release_dir}/out

rm -rf ${stage_dir} ${out_dir}
mkdir ${stage_dir} ${out_dir}

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
cp target/scala-2.11/variant-server*.war ${stage_dir}/variant-server-${full_version}.war

#
# JAVA CLIENT
#

cd ${workspace_root_dir}/CLIENT-JAVA
mvn clean package -DskipTests
cp target/variant-client*.jar ${stage_dir}
(cd src/main/java; jar -cvf ${stage_dir}/variant-client-adapter-source-${version}.jar com/variant/client/adapter/*)

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
tar -cvf ${out_dir}/variant-all-${full_version}.tar * #./*.jar ./*.war ./*.tar

#
# Javadoc
#
${release_dir}/bin/javadoc.sh