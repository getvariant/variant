#! /bin/bash
# Minify and add copyright notice at top.
#

version=${version:-unset}
cd $(dirname $0)/..
rm -rf target; mkdir target
java -jar yuicompressor-2.4.8.jar src/js/variant.js -o /tmp/package.js
(echo "/* Variant JavaScript Client release ${version} © Variant, Inc. All rights reserved. getvariant.com */"; cat /tmp/package.js) > target/variant-${version}.js
