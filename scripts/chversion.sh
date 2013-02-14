#!/bin/bash
# Takes two parameters, the 'from' and 'to' versions

FROMVER=$1
TESTFROMVER=`echo $FROMVER | egrep '*-SNAPSHOT'`
TOVER=$2
TESTTOVER=`echo $TOVER | egrep '*-SNAPSHOT'`

perl -pi -e "s/$FROMVER/$TOVER/g" `find . -name pom.xml`

