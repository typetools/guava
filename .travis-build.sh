#!/bin/bash

## This file compiles guava, using the development version of the
## Checker Framework (from GitHub) rather than a released version.

## Build Checker Framework
REPO=`/tmp/plume-scripts/git-find-fork ${SLUGOWNER} typetools checker-framework`
BRANCH=`/tmp/plume-scripts/git-find-branch ${REPO} ${TRAVIS_PULL_REQUEST_BRANCH:-$TRAVIS_BRANCH}`
(cd .. && git clone -b ${BRANCH} --single-branch --depth 1 -q ${REPO} checker-framework) || (cd .. && git clone -b ${BRANCH} --single-branch --depth 1 -q ${REPO} checker-framework)
# This also builds annotation-tools and jsr308-langtools
(cd $ROOT/checker-framework/ && ./.travis-build-without-test.sh downloadjdk)
export CHECKERFRAMEWORK=$ROOT/checker-framework

cd guava
mvn -P checkerframework-local -Dmaven.test.skip=true -Danimal.sniffer.skip=true
