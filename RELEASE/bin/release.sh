#! /bin/bash
# Make and package a Variant release
#
# 1. Core jar 

#!/bin/bash

export version=0.7.0
export version2="-RC3"

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
cd ${workspace_root_dir}/CORE
mvn clean package -DskipTests
#cp $workspace_root_dir/CORE/target/variant-core*.jar ${stage_dir}

#
# SERVER
#
cd ${workspace_root_dir}/SERVER
sbt clean dist
mv target/universal/variant-${version}.zip ${stage_dir}/server
cd ${stage_dir}/server
unzip variant-${version}.zip
rm variant-${version}.zip
cd variant-${version}
rm -rf README share
cp -r ${workspace_root_dir}/SERVER/distr/schemas .
mv bin/variant bin/playapp
mv bin/variant.bat bin/playapp.bat
cp ${workspace_root_dir}/SERVER/distr/bin/variant.sh bin
mkdir -p db/postgres db/h2
cp ${workspace_root_dir}/CORE/src/main/resources/variant/*schema.sql db/postgres
cp ${workspace_root_dir}/CORE/src/main/resources/variant/*schema.sql db/h2
cd ..
zip -r ${target_dir}/variant-${version}${version2}-server.zip variant-${version}/
cd ..
rm -rf server

#
# JAVA CLIENT & SERVLET ADAPTER
#
cd ${workspace_root_dir}/CLIENT-JAVA
mvn clean package -DskipTests
cp target/variant-java-client*.jar ${stage_dir}/java

cd ${workspace_root_dir}/CLIENT-JAVA-SERVLET
mvn clean package -DskipTests
cp target/variant-java-client-servlet-adapter*.jar ${stage_dir}/java

cd ${stage_dir}/java
zip ${target_dir}/variant-${version}${version2}-java.zip *.jar
cd ..
rm -rf java

#
# SERVLET DEMO
# Separate file because WP doesn't take files > 50M
# Zip up the demo WAR because WP doesn't take WAR files. 
#
cd ${workspace_root_dir}/CLIENT-JAVA-SERVLET-DEMO
mvn clean package -DskipTests
cd target
zip ${target_dir}/variant-${version}${version2}-java-servlet-demo.zip petclinic.war

#
# JAVASCRIPT CLIENT
#
${workspace_root_dir}/CLIENT-JS/bin/package.sh
cp ${workspace_root_dir}/CLIENT-JS/target/variant*.js ${target_dir}/variant-${version}${version2}.js

#
# Javadoc
#
#${release_dir}/bin/javadoc.sh