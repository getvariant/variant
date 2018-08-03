#! /bin/bash

#
# BUILD A PRODUCTION SERVER IN A GIVEN DIRECTORY
# USED FOR TESTING.
#

cd `dirname $0`/../../SERVER
root_dir=$(pwd)

# All args are required
if [ $# -ne 1 ]; then
    echo "usage: `basename $0` target-dir"
    exit 1
fi

server_dir=$1

#
# Main
#

# Preserve the target directory, if we're running inside an sbt forked test,
# as it will be blown away by release.sh
if [ -e target ]; then
    rm -rf /tmp/target
    mv target /tmp
fi
   
# Build the server
mbin/release.sh

# Unzip server to a temp directory
rm -rf ${server_dir}
mkdir ${server_dir}
unzip target/universal/variant-server-*.zip -d ${server_dir}
mv ${server_dir}/variant-server-*/* ${server_dir}
rmdir ${server_dir}/variant-server-*
cp standalone-server/conf/variant.conf ${server_dir}/conf
cp standalone-server/ext/* ${server_dir}/ext

# Restore the target directory.
if [ -e /tmp/target ]; then
    rm -rf target
    mv /tmp/target .
fi

