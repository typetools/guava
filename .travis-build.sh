#!/bin/bash

## This file compiles guava, using the development version of the
## Checker Framework (from GitHub) rather than a released version.

# Fail the whole script if any command fails
set -e

## Diagnostic output
# Output lines of this script as they are read.
set -o verbose
# Output expanded lines of this script as they are executed.
set -o xtrace

export SHELLOPTS

SLUGOWNER=${TRAVIS_PULL_REQUEST_SLUG%/*}
if [[ "$SLUGOWNER" == "" ]]; then
  SLUGOWNER=${TRAVIS_REPO_SLUG%/*}
fi
if [[ "$SLUGOWNER" == "" ]]; then
  SLUGOWNER=typetools
fi
echo SLUGOWNER=$SLUGOWNER

echo TRAVIS_PULL_REQUEST_BRANCH = $TRAVIS_PULL_REQUEST_BRANCH
echo TRAVIS_BRANCH = $TRAVIS_BRANCH

## Build Checker Framework
[ -d /tmp/plume-scripts ] || (cd /tmp && git clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git)
REPO=`/tmp/plume-scripts/git-find-fork ${SLUGOWNER} typetools checker-framework`
BRANCH=`/tmp/plume-scripts/git-find-branch ${REPO} ${TRAVIS_PULL_REQUEST_BRANCH:-$TRAVIS_BRANCH}`
(cd .. && git clone -b ${BRANCH} --single-branch --depth 1 -q ${REPO} checker-framework) || (cd .. && git clone -b ${BRANCH} --single-branch --depth 1 -q ${REPO} checker-framework)
# This also builds annotation-tools and jsr308-langtools
(cd ../checker-framework/ && ./.travis-build-without-test.sh downloadjdk)
pwd
export CHECKERFRAMEWORK=`readlink -f ../checker-framework`

ls $CHECKERFRAMEWORK/checker/dist

(cd guava && mvn package -P checkerframework-local -Dmaven.test.skip=true -Danimal.sniffer.skip=true)
