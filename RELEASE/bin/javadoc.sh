#!/bin/bash
#
# Generate Variant javadoc and copy it to the website.
# 


version=0.7.0
workspace_root_dir=$(cd $(dirname $0)/../..; pwd)
core_src_dir=${workspace_root_dir}/CORE/src/main/java
server_src_dir=${workspace_root_dir}/SERVER/app
client_src_dir=${workspace_root_dir}/CLIENT-JAVA/src/main/java
client_servlet_src_dir=${workspace_root_dir}/CLIENT-JAVA-SERVLET/src/main/java
javadoc_dir=${workspace_root_dir}/RELEASE/javadoc
target_file=${workspace_root_dir}/RELEASE/target/javadoc-${version}.tar

rm -rf ${javadoc_dir} ${target_dir}
mkdir ${target_dir} ${javadoc_dir}

javadoc -d ${javadoc_dir}  \
   -sourcepath ${core_src_dir}:${client_src_dir}:${client_servlet_src_dir}:${server_src_dir} \
   -windowtitle "Variant ${version}" \
   -doctitle "Variant Experiment Server Release ${version}" \
   -header "<a onclick=\"window.top.location.href='http://getvariant.com';\" href=\"#\"><img style=\"margin-bottom:5px;\" src=\"http://getvariant.com/wp-content/uploads/2016/05/VariantLogoSmall.png\"/></a>" \
   -bottom "Release $version. Updated $(date +"%d %b %y").<br/> Copyright &copy; 2017 Variant" \
   com.variant.core                   \
   com.variant.core.schema            \
   com.variant.client                 \
   com.variant.client.servlet         \
   com.variant.client.servlet.impl    \
   com.variant.client.servlet.util    \
   com.variant.server                 \

#
# Package
#
cd ${javadoc_dir}
tar -cvf ${target_file} *