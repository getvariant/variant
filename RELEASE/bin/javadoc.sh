#
# Generate Variant javadoc and copy it to the website.
#
# 1. Core jar 

#!/bin/bash

set +e
version="0.6.1"

workspace_root_dir=$(pwd)/$(dirname $0)/../..
core_src_dir=${workspace_root_dir}/CORE/src/main/java
client_src_dir=${workspace_root_dir}/CLIENT-JAVA/src/main/java
javadoc_dir=${workspace_root_dir}/RELEASE/javadoc
out_dir=${workspace_root_dir}/RELEASE/out

rm -rf ${javadoc_dir} ${out_dir}
mkdir ${out_dir} ${javadoc_dir}

javadoc -d ${javadoc_dir}  \
   -sourcepath ${core_src_dir}:${client_src_dir} \
   -windowtitle "Variant ${version}" \
   -doctitle "Variant Experiment Server Release ${version}" \
   -header "<a onclick=\"window.top.location.href='http://getvariant.com/docs';\" href=\"#\"><img src=\"http://getvariant.com/wp-content/uploads/2016/05/VariantLogoSmall.png\"/></a>" \
   -bottom "Release $version. Updated $(date +"%d %b %y").<br/> Copyright &copy; 2016 Variant" \
   com.variant.client                 \
   com.variant.client.servlet         \
   com.variant.client.servlet.impl    \
   com.variant.client.servlet.util    \
   com.variant.core                   \
   com.variant.core.util              \
   com.variant.core.event             \
   com.variant.core.hook              \
   com.variant.core.schema            \
   com.variant.core.schema.parser     \

#
# Package
#
cd ${javadoc_dir}
tar -cvf ${out_dir}/javadoc-${version}.tar *