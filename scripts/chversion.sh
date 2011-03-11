#!/bin/bash
# Script takes two parameters, the current version and the new version

perl -pi -e "s/$1/$2/g" `find . -name pom.xml -or -name MANIFEST.MF`
