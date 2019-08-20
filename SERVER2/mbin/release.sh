#! /bin/bash
# Package server
#

#!/bin/bash

export version=0.10.1
export version2=""

function usage() {
    echo "$(basename $0)"
} 

if [[ x != "x$1" ]]; then
    usage
    exit 1
fi

# Project root.
cd $(dirname $0)/..
project_root=$(pwd)

sbt clean dist

# extAPI jar is just our main jar, disguised under a different name. 
# Extract it from the ZIP archive.
cd target/universal
unzip -j variant-server-${version}.zip variant-server-0.10.1/lib/com.variant.variant-server-0.10.1.jar
mv com.variant.variant-server-0.10.1.jar variant-server-extapi-${version}.jar
