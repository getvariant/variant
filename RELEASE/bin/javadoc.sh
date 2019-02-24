#!/bin/bash
#
# Generate server and client javadoc.
# 

version=0.9
dot_version=0.10.0
workspace_root_dir=$(cd $(dirname $0)/../..; pwd)
core_src_dir=${workspace_root_dir}/CORE/src/main/java
server_src_dir=${workspace_root_dir}/SERVER/app
client_src_dir=${workspace_root_dir}/CLIENT-JAVA/src/main/java
javadoc_dir=${workspace_root_dir}/RELEASE/javadoc
target_file=${workspace_root_dir}/RELEASE/target/javadoc-${version}.tar

rm -rf ${javadoc_dir} ${target_dir}
mkdir ${target_dir} ${javadoc_dir}

javadoc -d ${javadoc_dir}  \
   -sourcepath ${core_src_dir}:${client_src_dir}:${server_src_dir} \
   -windowtitle "Variant Experience Server ${version} JavaDoc" \
   -doctitle "Variant Experiment Server ${version}" \
   -header "<a onclick=\"window.top.location.href='http://getvariant.com';\" href=\"#\"> <img style=\"margin-bottom:5px;\" src=\"http://getvariant.com/wp-content/uploads/2016/05/VariantLogoSmall.png\"/> \</a> \
   <script> \
    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){       \
    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),     \
    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)    \
    })(window,document,'script','https://www.google-analytics.com/analytics.js','ga'); \
    ga('create', 'UA-40337670-3', 'auto');                                             \
    ga('send', 'pageview');                                                            \
  </script>" \
   -bottom "Variant Experience Server release $dot_version. Updated $(date +"%d %b %Y").<br/> Copyright &copy; 2018 <a onclick=\"window.top.location.href='http://getvariant.com';\" href=\"#\">Variant Inc.</a>" \
   com.variant.core.lifecycle         \
   com.variant.core.schema            \
   com.variant.client                 \
   com.variant.client.lifecycle       \
   com.variant.server.api             \
   com.variant.server.api.lifecycle

#
# Package
#
cd ${javadoc_dir}
tar -cvf ${target_file} *