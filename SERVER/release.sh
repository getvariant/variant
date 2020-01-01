#! /bin/bash
# Package server
#

#!/bin/bash

export version=0.10.3
export version2=""

function usage() {
    echo "$(basename $0)"
} 

if [[ x != "x$1" ]]; then
    usage
    exit 1
fi

# Project root.
cd $(dirname $0)
project_root=$(pwd)

sbt clean dist

# extAPI jar is just our main jar, disguised under a different name and with the share jar added to it. 
# Extract it from the ZIP archive.
cd target/universal
unzip -j variant-server-${version}.zip variant-server-${version}/lib/com.variant.variant-server-${version}.jar
unzip -j variant-server-${version}.zip variant-server-${version}/lib/com.variant.variant-share-${version}.jar
mkdir tmp
(cd tmp; unzip -uo ../com.variant.variant-share-${version}.jar)
(cd tmp; unzip -uo ../com.variant.variant-server-${version}.jar)
jar -cvf variant-server-extapi-${version}.jar -C tmp .
rm -rf com.variant.variant-* tmp
