#! /bin/bash
# Package server
#

#!/bin/bash

export version=0.9.3
export version2=""

function usage() {
    echo "$(basename $0)"
} 

if [[ x != "x$1" ]]; then
    usage
    exit 1
fi

workspace_root_dir=$(cd $(dirname $0)/../..; pwd)

release_dir=${workspace_root_dir}/RELEASE
stage_dir=${release_dir}/stage
target_dir=${release_dir}/target

cd ${workspace_root_dir}/SERVER
sbt clean dist
cd target/universal
unzip variant-${version}.zip
rm variant-${version}.zip
cd variant-${version}
cp -r ${workspace_root_dir}/SERVER/distr/ext .
cp ${workspace_root_dir}/SERVER/distr/bin/variant.sh bin

# Rename play-built startup script in order not to confuse the customers.
mv bin/variant bin/playapp

# Make the log and schemata directories
mkdir log schemata

# Remove the auto-generated share directory 
rm -rf share

# Replace version in the control script. Used by "stop"
sed "s/<version>/${version}/" bin/variant.sh > /tmp/variant.sh
mv /tmp/variant.sh bin
chmod 751 bin/variant.sh

# HACK! Don't know how to do this cleaner, short of ditching Play.
# Add the ext/ directory to the server's classpath by directly manipulating the
# the play-built startup script.
sed 's/\(^declare \-r app_classpath\)\(.*\)\("$\)/\1\2:\$lib_dir\/..\/ext\/\*\3/' bin/playapp > /tmp/playapp
mv /tmp/playapp bin
chmod 751 bin/playapp

# extAPI jar is just our main jar, renamed.
cp lib/variant.variant-${version}-sans-externalized.jar ../variant-server-extapi-${version}.jar
cd ..
mv variant-${version} variant-server-${version}
zip -r variant-server-${version}.zip variant-server-${version}/
rm -rf variant-server-${version}
