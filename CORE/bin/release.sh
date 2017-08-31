#! /bin/bash
#
# Build core for release
#

workspace_root_dir=$(cd $(dirname $0)/../..; pwd)

cd ${workspace_root_dir}/CORE
mvn clean package -DskipTests


