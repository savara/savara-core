#!/bin/bash
# Hudson script for publishing the bundles to nexus

if [ "$RELEASE" == "No" ]; then
	mvn clean deploy -Dmaven.repo.local=${WORKSPACE}/m2-repository
fi
