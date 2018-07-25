#! /bin/bash

cd `dirname $0`/../../SERVER
root_dir=$(pwd)

# All args are required
if [ $# -ne 2 ]; then
    echo "usage: `basename $0` {build|start|stop} target-dir"
    exit 1
fi

server_dir=$2

#
# Main
#
case "$1" in
build)

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
    cp conf-test/standaloneServer.conf ${server_dir}/conf/variant.conf
    
    # Restore the target directory.
    if [ -e /tmp/target ]; then
        rm -rf target
        mv /tmp/target .
    fi

    ;;

start)
    # Start the server
    lsof -n -i4TCP:5377 | grep LISTEN | awk '{print $2}' | xargs kill
    ${server_dir}/bin/variant.sh start
    ;;

stop)
    # Stop the server
    ${server_dir}/bin/variant.sh stop
    ;;

*)
        echo "usage: `basename $0` {build|start|stop} target-dir"
        exit 1
esac
