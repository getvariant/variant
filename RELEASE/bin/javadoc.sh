#
# Generate Variant javadoc and copy it to the website.
#
# 1. Core jar 

#!/bin/bash

version=${version:-unset}
workspace_root_dir=$(cd $(dirname $0)/../..; pwd)
core_src_dir=${workspace_root_dir}/CORE/src/main/java
client_src_dir=${workspace_root_dir}/CLIENT-JAVA/src/main/java
javadoc_dir=${workspace_root_dir}/RELEASE/javadoc
target_file=${workspace_root_dir}/RELEASE/target/javadoc-${version}.tar

rm -rf ${javadoc_dir} ${target_dir}
mkdir ${target_dir} ${javadoc_dir}

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
   com.variant.core.xdm               \
   com.variant.core.schema            \

#
# Package
#
cd ${javadoc_dir}
tar -cvf ${target_file} *