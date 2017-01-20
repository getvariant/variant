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
rm -rf tmp
mkdir tmp
cd tmp 
unzip ../target/universal/variant-${version}.zip
rm -rf variant-${version}/README variant-${version}/share
mkdir variant-${version}/schemas 
cp -r ../schemas variant-${version}
cp ../conf/variant.sh variant-${version}/
#cp variant-${version}/bin/variant variant-${version}
#rm -rf variant-${version}/bin
zip -r ../variant-${version}.zip variant-${version}/ 

exit

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
tar -cvf ${target_dir}/variant-${version}-all.tar * #./*.jar ./*.war ./*.tar

#
# SERVLET DEMO
# Separate file because WP doesn't take files > 50M
# Tar up the demo WAR because WP doesn't take WAR files. 
#
cd ${workspace_root_dir}/CLIENT-JAVA-SERVLET-DEMO
mvn clean package -DskipTests
cd target
tar -cvf ${target_dir}/variant-${version}-java-servlet-demo.tar petclinic.war

#
# Javadoc
#
${release_dir}/bin/javadoc.sh