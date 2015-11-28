#
# Generate Variant javadoc and copy it to the website.
#
# 1. Core jar 

#!/bin/bash

set +e
set -x
version="0.5"

workspace_root_dir=$(pwd)/$(dirname $0)/../..
core_src_dir=${workspace_root_dir}/CORE/src/main/java
web_src_dir=${workspace_root_dir}/WEB/src/main/java
javadoc_dir=${workspace_root_dir}/RELEASE/javadoc
out_dir=${workspace_root_dir}/RELEASE/out

rm -rf ${javadoc_dir} ${out_dir}
mkdir ${out_dir}

javadoc -d ${javadoc_dir}  \
   -sourcepath ${core_src_dir}:${web_src_dir} \
   -windowtitle "Variant RCE Container 0.5 Java API" \
   -doctitle "Variant Randomized Controlled Experiment Container Java API<br/>Version 0.5" \
   -bottom "Copyright &copy; 2015 Variant"  \
   com.variant.core                 \
   com.variant.core.event           \
   com.variant.core.flashpoint      \
   com.variant.core.schema          \
   com.variant.core.schema.parser   \
   com.variant.web

#
# Package
#
cd ${javadoc_dir}
tar -cvf ${out_dir}/javadoc-${version}.tar *