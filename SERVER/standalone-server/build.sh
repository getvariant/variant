#! /bin/bash

#
# BUILD A PRODUCTION SERVER IN A GIVEN DIRECTORY.
# GETS ALL SCHEATA FROM SCHEMATA-TEST
# USED FOR TESTING.  
#

cd `dirname $0`/..
root_dir=$(pwd)

# All args are required
if [ $# -ne 2 ]; then
    echo "usage: `basename $0` target-dir {postgres|mysql|none}" >&2
    echo "but was: `basename $0` $@" >&2
    exit 1
fi

server_dir=$1
flusher=$2

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
./release.sh

# Unzip server to a temp directory
rm -rf ${server_dir}
mkdir ${server_dir}
unzip target/universal/variant-server-*.zip -d ${server_dir}
mv ${server_dir}/variant-server-*/* ${server_dir}
rmdir ${server_dir}/variant-server-*

if [ "$flusher" != "none" ]; then
  cp standalone-server/conf/variant-${flusher}.conf ${server_dir}/conf/variant.conf
fi

cp standalone-server/ext/* ${server_dir}/ext

# Do we need to copy any schemata?
#cp schemata-test/* ${server_dir}/schemata

# Restore the target directory.
if [ -e /tmp/target ]; then
    rm -rf target
    mv /tmp/target .
fi

